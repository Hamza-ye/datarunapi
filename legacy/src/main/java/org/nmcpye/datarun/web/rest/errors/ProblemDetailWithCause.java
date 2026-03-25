package org.nmcpye.datarun.web.rest.errors;

import org.springframework.http.ProblemDetail;
import java.net.URI;

public class ProblemDetailWithCause extends ProblemDetail {

    private ProblemDetailWithCause cause;

    public ProblemDetailWithCause() {
        super();
    }

    public ProblemDetailWithCause(int rawStatusCode) {
        super(rawStatusCode);
    }

    public ProblemDetailWithCause getCause() {
        return cause;
    }

    public void setCause(ProblemDetailWithCause cause) {
        this.cause = cause;
    }

    public static class ProblemDetailWithCauseBuilder {

        private int status;
        private URI type;
        private String title;
        private String detail;
        private URI instance;
        private ProblemDetailWithCause cause;
        private java.util.Map<String, Object> properties = new java.util.HashMap<>();

        public static ProblemDetailWithCauseBuilder instance() {
            return new ProblemDetailWithCauseBuilder();
        }

        public ProblemDetailWithCauseBuilder withStatus(int status) {
            this.status = status;
            return this;
        }

        public ProblemDetailWithCauseBuilder withType(URI type) {
            this.type = type;
            return this;
        }

        public ProblemDetailWithCauseBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public ProblemDetailWithCauseBuilder withDetail(String detail) {
            this.detail = detail;
            return this;
        }

        public ProblemDetailWithCauseBuilder withInstance(URI instance) {
            this.instance = instance;
            return this;
        }

        public ProblemDetailWithCauseBuilder withCause(ProblemDetailWithCause cause) {
            this.cause = cause;
            return this;
        }

        public ProblemDetailWithCauseBuilder withProperty(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public ProblemDetailWithCause build() {
            ProblemDetailWithCause problem = new ProblemDetailWithCause();
            problem.setStatus(this.status);
            problem.setType(this.type);
            problem.setTitle(this.title);
            problem.setDetail(this.detail);
            problem.setInstance(this.instance);
            problem.setCause(this.cause);
            for (java.util.Map.Entry<String, Object> entry : this.properties.entrySet()) {
                problem.setProperty(entry.getKey(), entry.getValue());
            }
            return problem;
        }
    }
}
