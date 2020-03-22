// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nuxeo.proto

package org.nuxeo.micro.repo.service.grpc;

public interface DocumentOrBuilder extends
    // @@protoc_insertion_point(interface_extends:NuxeoClient.Document)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string repositoryName = 1;</code>
   */
  java.lang.String getRepositoryName();
  /**
   * <code>string repositoryName = 1;</code>
   */
  com.google.protobuf.ByteString
      getRepositoryNameBytes();

  /**
   * <code>string uid = 2;</code>
   */
  java.lang.String getUid();
  /**
   * <code>string uid = 2;</code>
   */
  com.google.protobuf.ByteString
      getUidBytes();

  /**
   * <code>string path = 3;</code>
   */
  java.lang.String getPath();
  /**
   * <code>string path = 3;</code>
   */
  com.google.protobuf.ByteString
      getPathBytes();

  /**
   * <code>string type = 4;</code>
   */
  java.lang.String getType();
  /**
   * <code>string type = 4;</code>
   */
  com.google.protobuf.ByteString
      getTypeBytes();

  /**
   * <code>repeated string facets = 5;</code>
   */
  java.util.List<java.lang.String>
      getFacetsList();
  /**
   * <code>repeated string facets = 5;</code>
   */
  int getFacetsCount();
  /**
   * <code>repeated string facets = 5;</code>
   */
  java.lang.String getFacets(int index);
  /**
   * <code>repeated string facets = 5;</code>
   */
  com.google.protobuf.ByteString
      getFacetsBytes(int index);

  /**
   * <code>repeated .NuxeoClient.Document.Schema schema = 6;</code>
   */
  java.util.List<org.nuxeo.micro.repo.service.grpc.Document.Schema> 
      getSchemaList();
  /**
   * <code>repeated .NuxeoClient.Document.Schema schema = 6;</code>
   */
  org.nuxeo.micro.repo.service.grpc.Document.Schema getSchema(int index);
  /**
   * <code>repeated .NuxeoClient.Document.Schema schema = 6;</code>
   */
  int getSchemaCount();
  /**
   * <code>repeated .NuxeoClient.Document.Schema schema = 6;</code>
   */
  java.util.List<? extends org.nuxeo.micro.repo.service.grpc.Document.SchemaOrBuilder> 
      getSchemaOrBuilderList();
  /**
   * <code>repeated .NuxeoClient.Document.Schema schema = 6;</code>
   */
  org.nuxeo.micro.repo.service.grpc.Document.SchemaOrBuilder getSchemaOrBuilder(
      int index);

  /**
   * <code>string state = 7;</code>
   */
  java.lang.String getState();
  /**
   * <code>string state = 7;</code>
   */
  com.google.protobuf.ByteString
      getStateBytes();

  /**
   * <code>string parentRef = 8;</code>
   */
  java.lang.String getParentRef();
  /**
   * <code>string parentRef = 8;</code>
   */
  com.google.protobuf.ByteString
      getParentRefBytes();

  /**
   * <code>bool isCheckedOut = 9;</code>
   */
  boolean getIsCheckedOut();

  /**
   * <code>string changeToken = 10;</code>
   */
  java.lang.String getChangeToken();
  /**
   * <code>string changeToken = 10;</code>
   */
  com.google.protobuf.ByteString
      getChangeTokenBytes();

  /**
   * <code>string title = 11;</code>
   */
  java.lang.String getTitle();
  /**
   * <code>string title = 11;</code>
   */
  com.google.protobuf.ByteString
      getTitleBytes();

  /**
   * <code>int64 lastModified = 12;</code>
   */
  long getLastModified();

  /**
   * <code>string versionLabel = 13;</code>
   */
  java.lang.String getVersionLabel();
  /**
   * <code>string versionLabel = 13;</code>
   */
  com.google.protobuf.ByteString
      getVersionLabelBytes();

  /**
   * <code>string lockOwner = 14;</code>
   */
  java.lang.String getLockOwner();
  /**
   * <code>string lockOwner = 14;</code>
   */
  com.google.protobuf.ByteString
      getLockOwnerBytes();

  /**
   * <code>bool lockCreated = 15;</code>
   */
  boolean getLockCreated();

  /**
   * <code>map&lt;string, .NuxeoClient.Document.DataModel&gt; data = 16;</code>
   */
  int getDataCount();
  /**
   * <code>map&lt;string, .NuxeoClient.Document.DataModel&gt; data = 16;</code>
   */
  boolean containsData(
      java.lang.String key);
  /**
   * Use {@link #getDataMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, org.nuxeo.micro.repo.service.grpc.Document.DataModel>
  getData();
  /**
   * <code>map&lt;string, .NuxeoClient.Document.DataModel&gt; data = 16;</code>
   */
  java.util.Map<java.lang.String, org.nuxeo.micro.repo.service.grpc.Document.DataModel>
  getDataMap();
  /**
   * <code>map&lt;string, .NuxeoClient.Document.DataModel&gt; data = 16;</code>
   */

  org.nuxeo.micro.repo.service.grpc.Document.DataModel getDataOrDefault(
      java.lang.String key,
      org.nuxeo.micro.repo.service.grpc.Document.DataModel defaultValue);
  /**
   * <code>map&lt;string, .NuxeoClient.Document.DataModel&gt; data = 16;</code>
   */

  org.nuxeo.micro.repo.service.grpc.Document.DataModel getDataOrThrow(
      java.lang.String key);
}
