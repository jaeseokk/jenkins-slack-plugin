package jenkins.plugins.line;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings({ "unchecked" })
public class LineNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(LineNotifier.class.getName());

    private String accessToken;

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public String getAccessToken() {
        return accessToken;
    }

    @DataBoundConstructor
    public LineNotifier(final String accessToken) {
        super();
        this.accessToken = accessToken;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public LineService newLineService(final String room) {
        return new StandardLineService(getAccessToken());
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        return true;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String accessToken;

        public DescriptorImpl() {
            load();
        }

        public String getAccessToken() {
            return accessToken;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public LineNotifier newInstance(StaplerRequest sr) {
            if (accessToken == null) {
                accessToken = sr.getParameter("accessToken");
            }
            return new LineNotifier(accessToken);
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            accessToken = sr.getParameter("accessToken");
            try {
                new LineNotifier(accessToken);
            } catch (Exception e) {
                throw new FormException(
                        "Failed to initialize notifier - check your global notifier configuration settings", e, "");
            }
            save();
            return super.configure(sr, formData);
        }

        @Override
        public String getDisplayName() {
            return "Line Notifications";
        }
    }

    public static class LineJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {
        private String accessToken;
        private boolean startNotification;
        private boolean notifySuccess;
        private boolean notifyAborted;
        private boolean notifyNotBuilt;
        private boolean notifyUnstable;
        private boolean notifyFailure;
        private boolean notifyBackToNormal;

        @DataBoundConstructor
        public LineJobProperty(String accessToken, boolean startNotification, boolean notifyAborted,
                boolean notifyFailure, boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable,
                boolean notifyBackToNormal) {
            this.accessToken = accessToken;
            this.startNotification = startNotification;
            this.notifyAborted = notifyAborted;
            this.notifyFailure = notifyFailure;
            this.notifyNotBuilt = notifyNotBuilt;
            this.notifySuccess = notifySuccess;
            this.notifyUnstable = notifyUnstable;
            this.notifyBackToNormal = notifyBackToNormal;
        }

        @Exported
        public String getAccessToken() {
            return accessToken;
        }

        @Exported
        public boolean getStartNotification() {
            return startNotification;
        }

        @Exported
        public boolean getNotifySuccess() {
            return notifySuccess;
        }

        @Override
        public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
            if (startNotification) {
                Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
                for (Publisher publisher : map.values()) {
                    if (publisher instanceof LineNotifier) {
                        logger.info("Invoking Started...");
                        new ActiveNotifier((LineNotifier) publisher).started(build);
                    }
                }
            }
            return super.prebuild(build, listener);
        }

        @Exported
        public boolean getNotifyAborted() {
            return notifyAborted;
        }

        @Exported
        public boolean getNotifyFailure() {
            return notifyFailure;
        }

        @Exported
        public boolean getNotifyNotBuilt() {
            return notifyNotBuilt;
        }

        @Exported
        public boolean getNotifyUnstable() {
            return notifyUnstable;
        }

        @Exported
        public boolean getNotifyBackToNormal() {
            return notifyBackToNormal;
        }

        @Extension
        public static final class DescriptorImpl extends JobPropertyDescriptor {
            public String getDisplayName() {
                return "Slack Notifications";
            }

            @Override
            public boolean isApplicable(Class<? extends Job> jobType) {
                return true;
            }

            @Override
            public LineJobProperty newInstance(StaplerRequest sr, JSONObject formData)
                    throws hudson.model.Descriptor.FormException {
                return new LineJobProperty(sr.getParameter("slackProjectRoom"),
                        sr.getParameter("slackStartNotification") != null,
                        sr.getParameter("slackNotifyAborted") != null, sr.getParameter("slackNotifyFailure") != null,
                        sr.getParameter("slackNotifyNotBuilt") != null, sr.getParameter("slackNotifySuccess") != null,
                        sr.getParameter("slackNotifyUnstable") != null,
                        sr.getParameter("slackNotifyBackToNormal") != null);
            }
        }
    }
}
