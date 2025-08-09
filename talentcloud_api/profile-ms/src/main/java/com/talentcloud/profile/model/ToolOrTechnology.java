package com.talentcloud.profile.model;

public enum ToolOrTechnology {
    DOCKER("Docker"),
    KUBERNETES("Kubernetes"),
    AWS("Amazon Web Services"),
    AZURE("Microsoft Azure"),
    GCP("Google Cloud Platform"),
    JENKINS("Jenkins"),
    GIT("Git"),
    GITHUB_ACTIONS("GitHub Actions"),
    TERRAFORM("Terraform"),
    ANSIBLE("Ansible"),
    POSTMAN("Postman"),
    FIGMA("Figma"),
    SELENIUM("Selenium"),
    CYPRESS("Cypress"),
    ELK_STACK("ELK Stack"),
    PROMETHEUS("Prometheus"),
    GRAFANA("Grafana"),
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    MONGODB("MongoDB"),
    REDIS("Redis"),
    APACHE_KAFKA("Apache Kafka"),
    APACHE_SPARK("Apache Spark"),
    FLINK("Apache Flink"),
    HADOOP("Hadoop"),
    LANGCHAIN("LangChain"),
    OPENAI_API("OpenAI API"),
    TENSORFLOW("TensorFlow"),
    PYTORCH("PyTorch"),
    JIRA("Jira"),
    CONFLUENCE("Confluence"),
    TABLEAU("Tableau"),
    POWER_BI("Power BI"),
    STREAMLIT("Streamlit"),
    NEXT_JS("Next.js"),
    REACT("React"),
    ANGULAR("Angular"),
    VUE_JS("Vue.js"),
    NGINX("Nginx");

    private final String tool;

    ToolOrTechnology(String tool) {
        this.tool = tool;
    }

    public String getTool() {
        return tool;
    }
}