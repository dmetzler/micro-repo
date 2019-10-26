package org.nuxeo.micro;

import java.io.File;

import org.apache.commons.io.FileCleaningTracker;

public class FileTracker {

    private static final FileCleaningTracker tracker = new FileCleaningTracker();

    public static void track(File file, Object marker) {
        tracker.track(file, marker);
    }
}
