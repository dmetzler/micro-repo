/*
 * (C) Copyright 2015-2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.blob;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.blob.BlobDispatcher.BlobDispatch;
import org.nuxeo.ecm.core.blob.binary.BinaryGarbageCollector;
import org.nuxeo.ecm.core.blob.binary.BinaryManager;
import org.nuxeo.ecm.core.blob.binary.BinaryManagerStatus;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.model.Document.BlobAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the service managing {@link Blob}s associated to a
 * {@link Document} or a repository.
 *
 * @since 9.2
 */
public class DocumentBlobManagerImpl implements DocumentBlobManager {

    private static final Logger log = LoggerFactory.getLogger(DocumentBlobManagerImpl.class);

    protected static final String XP = "configuration";

    protected static BlobDispatcher DEFAULT_BLOB_DISPATCHER = new DefaultBlobDispatcher();

    protected Deque<BlobDispatcherDescriptor> blobDispatcherDescriptorsRegistry = new LinkedList<>();

    private BlobManager blobManager;


    public DocumentBlobManagerImpl(BlobManager blobManager) {
        this.blobManager = blobManager;
    }

    public void deactivate() {
        blobDispatcherDescriptorsRegistry.clear();
    }

    protected void registerBlobDispatcher(BlobDispatcherDescriptor descr) {
        blobDispatcherDescriptorsRegistry.add(descr);
    }

    protected void unregisterBlobDispatcher(BlobDispatcherDescriptor descr) {
        blobDispatcherDescriptorsRegistry.remove(descr);
    }

    protected BlobDispatcher getBlobDispatcher() {
        BlobDispatcherDescriptor descr = blobDispatcherDescriptorsRegistry.peekLast();
        if (descr == null) {
            return DEFAULT_BLOB_DISPATCHER;
        }
        return descr.getBlobDispatcher();
    }

    protected BlobProvider getBlobProvider(String providerId) {
        return blobManager.getBlobProvider(providerId);
    }

    protected DocumentBlobProvider getDocumentBlobProvider(Blob blob) {
        BlobProvider blobProvider = blobManager.getBlobProvider(blob);
        if (blobProvider instanceof DocumentBlobProvider) {
            return (DocumentBlobProvider) blobProvider;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@link BlobInfo} (coming from the database) contains the blob key, which
     * may or may not be prefixed by a blob provider id.
     */
    @Override
    public Blob readBlob(BlobInfo blobInfo, String repositoryName) throws IOException {
        String key = blobInfo.key;
        if (key == null) {
            return null;
        }
        BlobProvider blobProvider = getBlobProvider(key, repositoryName);
        if (blobProvider == null) {
            throw new NuxeoException("No registered blob provider for key: " + key);
        }
        return blobProvider.readBlob(blobInfo);
    }

    protected BlobProvider getBlobProvider(String key, String repositoryName) {
        int colon = key.indexOf(':');
        String providerId;
        if (colon < 0) {
            // no prefix, use the blob dispatcher to find the blob provider id
            providerId = getBlobDispatcher().getBlobProvider(repositoryName);
        } else {
            // use the prefix as blob provider id
            providerId = key.substring(0, colon);
        }
        return getBlobProvider(providerId);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the blob is managed and already uses the provider that's expected for this
     * blob and document, there is no need to recompute a key. Otherwise, go through
     * the blob provider.
     */
    @Override
    public String writeBlob(Blob blob, Document doc, String xpath) throws IOException {
        BlobDispatcher blobDispatcher = getBlobDispatcher();
        BlobDispatch dispatch = null;
        if (blob instanceof ManagedBlob) {
            ManagedBlob managedBlob = (ManagedBlob) blob;
            String currentProviderId = managedBlob.getProviderId();
            // is the blob non-transient, so that reusing the key is an option?
            if (!getBlobProvider(currentProviderId).isTransient()) {
                // is it something we don't have to dispatch?
                if (!blobDispatcher.getBlobProviderIds().contains(currentProviderId)) {
                    // not something we have to dispatch, reuse the key
                    return managedBlob.getKey();
                }
                dispatch = blobDispatcher.getBlobProvider(doc, blob, xpath);
                if (dispatch.providerId.equals(currentProviderId)) {
                    // same provider, just reuse the key
                    return managedBlob.getKey();
                }
            }
        }
        if (dispatch == null) {
            dispatch = blobDispatcher.getBlobProvider(doc, blob, xpath);
        }
        BlobProvider blobProvider = getBlobProvider(dispatch.providerId);
        if (blobProvider == null) {
            throw new NuxeoException("No registered blob provider with id: " + dispatch.providerId);
        }
        String key = blobProvider.writeBlob(blob);
        if (dispatch.addPrefix) {
            key = dispatch.providerId + ':' + key;
        }
        return key;
    }

    @Override
    public InputStream getConvertedStream(Blob blob, String mimeType, DocumentModel doc) throws IOException {
        DocumentBlobProvider blobProvider = getDocumentBlobProvider(blob);
        if (blobProvider == null) {
            return null;
        }
        return blobProvider.getConvertedStream((ManagedBlob) blob, mimeType, doc);
    }

    protected void freezeVersion(BlobAccessor accessor, Document doc) {
        Blob blob = accessor.getBlob();
        DocumentBlobProvider blobProvider = getDocumentBlobProvider(blob);
        if (blobProvider == null) {
            return;
        }
        try {
            Blob newBlob = blobProvider.freezeVersion((ManagedBlob) blob, doc);
            if (newBlob != null) {
                accessor.setBlob(newBlob);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void freezeVersion(Document doc) {
        // finds all blobs, then ask their providers if there's anything to do on check
        // in
        doc.visitBlobs(accessor -> freezeVersion(accessor, doc));
    }

    @Override
    public void notifyChanges(Document doc, Set<String> xpaths) {
        getBlobDispatcher().notifyChanges(doc, xpaths);
    }

    // find which GCs to use
    // only GC the binary managers to which we dispatch blobs
    protected List<BinaryGarbageCollector> getGarbageCollectors() {
        List<BinaryGarbageCollector> gcs = new LinkedList<>();
        for (String providerId : getBlobDispatcher().getBlobProviderIds()) {
            BlobProvider blobProvider = getBlobProvider(providerId);
            BinaryManager binaryManager = blobProvider.getBinaryManager();
            if (binaryManager != null) {
                gcs.add(binaryManager.getGarbageCollector());
            }
        }
        return gcs;
    }

    @Override
    public BinaryManagerStatus garbageCollectBinaries(boolean delete) {
        List<BinaryGarbageCollector> gcs = getGarbageCollectors();
        // start gc
        long start = System.currentTimeMillis();
        for (BinaryGarbageCollector gc : gcs) {
            gc.start();
        }
        // in all repositories, mark referenced binaries
        // the marking itself will call back into the appropriate gc's mark method
//        for (String repositoryName : repositoryService.getRepositoryNames()) {
//            Repository repository = repositoryService.getRepository(repositoryName);
//            repository.markReferencedBinaries();
//        }
        // stop gc
        BinaryManagerStatus globalStatus = new BinaryManagerStatus();
        for (BinaryGarbageCollector gc : gcs) {
            gc.stop(delete);
            BinaryManagerStatus status = gc.getStatus();
            globalStatus.numBinaries += status.numBinaries;
            globalStatus.sizeBinaries += status.sizeBinaries;
            globalStatus.numBinariesGC += status.numBinariesGC;
            globalStatus.sizeBinariesGC += status.sizeBinariesGC;
        }
        globalStatus.gcDuration = System.currentTimeMillis() - start;
        return globalStatus;
    }

    @Override
    public void markReferencedBinary(String key, String repositoryName) {
        BlobProvider blobProvider = getBlobProvider(key, repositoryName);
        BinaryManager binaryManager = blobProvider.getBinaryManager();
        if (binaryManager != null) {
            int colon = key.indexOf(':');
            if (colon > 0) {
                // if the key is in the "providerId:digest" format, keep only the real digest
                key = key.substring(colon + 1);
            }
            binaryManager.getGarbageCollector().mark(key);
        } else {
            log.error("Unknown binary manager for key: " + key);
        }
    }

    @Override
    public boolean isBinariesGarbageCollectionInProgress() {
        for (BinaryGarbageCollector gc : getGarbageCollectors()) {
            if (gc.isInProgress()) {
                return true;
            }
        }
        return false;
    }

}
