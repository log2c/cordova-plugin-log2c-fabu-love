package com.log2c.cordova.plugin.fabulove;

import java.util.List;

public class ResponseModel {
    private boolean success;
    private DataBean data;

    @Override
    public String toString() {
        return "ResponseModel{" +
                "success=" + success +
                ", data=" + data +
                '}';
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private AppBean app;
        private VersionBean version;

        @Override
        public String toString() {
            return "DataBean{" +
                    "app=" + app +
                    ", version=" + version +
                    '}';
        }

        public AppBean getApp() {
            return app;
        }

        public void setApp(AppBean app) {
            this.app = app;
        }

        public VersionBean getVersion() {
            return version;
        }

        public void setVersion(VersionBean version) {
            this.version = version;
        }

        public static class AppBean {
            private TodayDownloadCountBean todayDownloadCount;
            private GrayStrategyBean grayStrategy;
            private boolean autoPublish;
            private String updateMode;
            private int totalDownloadCount;
            private String _id;
            private String appName;
            private String bundleId;
            private String platform;
            private String creator;
            private String creatorId;
            private String icon;
            private String shortUrl;
            private String createAt;
            private String ownerId;
            private String currentVersion;
            private int __v;
            private String releaseVersionCode;
            private String releaseVersionId;

            @Override
            public String toString() {
                return "AppBean{" +
                        "todayDownloadCount=" + todayDownloadCount +
                        ", grayStrategy=" + grayStrategy +
                        ", autoPublish=" + autoPublish +
                        ", updateMode='" + updateMode + '\'' +
                        ", totalDownloadCount=" + totalDownloadCount +
                        ", _id='" + _id + '\'' +
                        ", appName='" + appName + '\'' +
                        ", bundleId='" + bundleId + '\'' +
                        ", platform='" + platform + '\'' +
                        ", creator='" + creator + '\'' +
                        ", creatorId='" + creatorId + '\'' +
                        ", icon='" + icon + '\'' +
                        ", shortUrl='" + shortUrl + '\'' +
                        ", createAt='" + createAt + '\'' +
                        ", ownerId='" + ownerId + '\'' +
                        ", currentVersion='" + currentVersion + '\'' +
                        ", __v=" + __v +
                        ", releaseVersionCode='" + releaseVersionCode + '\'' +
                        ", releaseVersionId='" + releaseVersionId + '\'' +
                        '}';
            }

            public TodayDownloadCountBean getTodayDownloadCount() {
                return todayDownloadCount;
            }

            public void setTodayDownloadCount(TodayDownloadCountBean todayDownloadCount) {
                this.todayDownloadCount = todayDownloadCount;
            }

            public GrayStrategyBean getGrayStrategy() {
                return grayStrategy;
            }

            public void setGrayStrategy(GrayStrategyBean grayStrategy) {
                this.grayStrategy = grayStrategy;
            }

            public boolean isAutoPublish() {
                return autoPublish;
            }

            public void setAutoPublish(boolean autoPublish) {
                this.autoPublish = autoPublish;
            }

            public String getUpdateMode() {
                return updateMode;
            }

            public void setUpdateMode(String updateMode) {
                this.updateMode = updateMode;
            }

            public int getTotalDownloadCount() {
                return totalDownloadCount;
            }

            public void setTotalDownloadCount(int totalDownloadCount) {
                this.totalDownloadCount = totalDownloadCount;
            }

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public String getAppName() {
                return appName;
            }

            public void setAppName(String appName) {
                this.appName = appName;
            }

            public String getBundleId() {
                return bundleId;
            }

            public void setBundleId(String bundleId) {
                this.bundleId = bundleId;
            }

            public String getPlatform() {
                return platform;
            }

            public void setPlatform(String platform) {
                this.platform = platform;
            }

            public String getCreator() {
                return creator;
            }

            public void setCreator(String creator) {
                this.creator = creator;
            }

            public String getCreatorId() {
                return creatorId;
            }

            public void setCreatorId(String creatorId) {
                this.creatorId = creatorId;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public String getShortUrl() {
                return shortUrl;
            }

            public void setShortUrl(String shortUrl) {
                this.shortUrl = shortUrl;
            }

            public String getCreateAt() {
                return createAt;
            }

            public void setCreateAt(String createAt) {
                this.createAt = createAt;
            }

            public String getOwnerId() {
                return ownerId;
            }

            public void setOwnerId(String ownerId) {
                this.ownerId = ownerId;
            }

            public String getCurrentVersion() {
                return currentVersion;
            }

            public void setCurrentVersion(String currentVersion) {
                this.currentVersion = currentVersion;
            }

            public int get__v() {
                return __v;
            }

            public void set__v(int __v) {
                this.__v = __v;
            }

            public String getReleaseVersionCode() {
                return releaseVersionCode;
            }

            public void setReleaseVersionCode(String releaseVersionCode) {
                this.releaseVersionCode = releaseVersionCode;
            }

            public String getReleaseVersionId() {
                return releaseVersionId;
            }

            public void setReleaseVersionId(String releaseVersionId) {
                this.releaseVersionId = releaseVersionId;
            }

            public static class TodayDownloadCountBean {
                private int count;
                private String date;

                @Override
                public String toString() {
                    return "TodayDownloadCountBean{" +
                            "count=" + count +
                            ", date='" + date + '\'' +
                            '}';
                }

                public int getCount() {
                    return count;
                }

                public void setCount(int count) {
                    this.count = count;
                }

                public String getDate() {
                    return date;
                }

                public void setDate(String date) {
                    this.date = date;
                }
            }

            public static class GrayStrategyBean {
                private String ipType;
                private String updateMode;
                private List<?> ipList;

                @Override
                public String toString() {
                    return "GrayStrategyBean{" +
                            "ipType='" + ipType + '\'' +
                            ", updateMode='" + updateMode + '\'' +
                            ", ipList=" + ipList +
                            '}';
                }

                public String getIpType() {
                    return ipType;
                }

                public void setIpType(String ipType) {
                    this.ipType = ipType;
                }

                public String getUpdateMode() {
                    return updateMode;
                }

                public void setUpdateMode(String updateMode) {
                    this.updateMode = updateMode;
                }

                public List<?> getIpList() {
                    return ipList;
                }

                public void setIpList(List<?> ipList) {
                    this.ipList = ipList;
                }
            }
        }

        public static class VersionBean {
            private int downloadCount;
            private boolean showOnDownloadPage;
            private boolean hidden;
            private String updateMode;
            private String _id;
            private String versionCode;
            private String bundleId;
            private String versionStr;
            private String downloadUrl;
            private String uploader;
            private String uploaderId;
            private int size;
            private String uploadAt;
            private String appId;
            private String installUrl;
            private int __v;
            private String changelog;

            @Override
            public String toString() {
                return "VersionBean{" +
                        "downloadCount=" + downloadCount +
                        ", showOnDownloadPage=" + showOnDownloadPage +
                        ", hidden=" + hidden +
                        ", updateMode='" + updateMode + '\'' +
                        ", _id='" + _id + '\'' +
                        ", versionCode='" + versionCode + '\'' +
                        ", bundleId='" + bundleId + '\'' +
                        ", versionStr='" + versionStr + '\'' +
                        ", downloadUrl='" + downloadUrl + '\'' +
                        ", uploader='" + uploader + '\'' +
                        ", uploaderId='" + uploaderId + '\'' +
                        ", size=" + size +
                        ", uploadAt='" + uploadAt + '\'' +
                        ", appId='" + appId + '\'' +
                        ", installUrl='" + installUrl + '\'' +
                        ", __v=" + __v +
                        ", changelog='" + changelog + '\'' +
                        '}';
            }

            public int getDownloadCount() {
                return downloadCount;
            }

            public void setDownloadCount(int downloadCount) {
                this.downloadCount = downloadCount;
            }

            public boolean isShowOnDownloadPage() {
                return showOnDownloadPage;
            }

            public void setShowOnDownloadPage(boolean showOnDownloadPage) {
                this.showOnDownloadPage = showOnDownloadPage;
            }

            public boolean isHidden() {
                return hidden;
            }

            public void setHidden(boolean hidden) {
                this.hidden = hidden;
            }

            public String getUpdateMode() {
                return updateMode;
            }

            public void setUpdateMode(String updateMode) {
                this.updateMode = updateMode;
            }

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public String getVersionCode() {
                return versionCode;
            }

            public void setVersionCode(String versionCode) {
                this.versionCode = versionCode;
            }

            public String getBundleId() {
                return bundleId;
            }

            public void setBundleId(String bundleId) {
                this.bundleId = bundleId;
            }

            public String getVersionStr() {
                return versionStr;
            }

            public void setVersionStr(String versionStr) {
                this.versionStr = versionStr;
            }

            public String getDownloadUrl() {
                return downloadUrl;
            }

            public void setDownloadUrl(String downloadUrl) {
                this.downloadUrl = downloadUrl;
            }

            public String getUploader() {
                return uploader;
            }

            public void setUploader(String uploader) {
                this.uploader = uploader;
            }

            public String getUploaderId() {
                return uploaderId;
            }

            public void setUploaderId(String uploaderId) {
                this.uploaderId = uploaderId;
            }

            public int getSize() {
                return size;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public String getUploadAt() {
                return uploadAt;
            }

            public void setUploadAt(String uploadAt) {
                this.uploadAt = uploadAt;
            }

            public String getAppId() {
                return appId;
            }

            public void setAppId(String appId) {
                this.appId = appId;
            }

            public String getInstallUrl() {
                return installUrl;
            }

            public void setInstallUrl(String installUrl) {
                this.installUrl = installUrl;
            }

            public int get__v() {
                return __v;
            }

            public void set__v(int __v) {
                this.__v = __v;
            }

            public String getChangelog() {
                return changelog;
            }

            public void setChangelog(String changelog) {
                this.changelog = changelog;
            }
        }
    }
}
