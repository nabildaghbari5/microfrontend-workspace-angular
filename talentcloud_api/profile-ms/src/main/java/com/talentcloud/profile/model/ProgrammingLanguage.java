package com.talentcloud.profile.model;


public enum ProgrammingLanguage {
    JAVA("Java"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript"),
    TYPESCRIPT("TypeScript"),
    C_SHARP("C#"),
    C_PLUS_PLUS("C++"),
    PHP("PHP"),
    RUBY("Ruby"),
    SWIFT("Swift"),
    KOTLIN("Kotlin"),
    GO("Go"),
    RUST("Rust"),
    SCALA("Scala"),
    R("R"),
    PERL("Perl"),
    DART("Dart"),
    SHELL_BASH("Shell/Bash"),
    SQL("SQL"),
    HTML_CSS("HTML/CSS"),
    ASSEMBLY("Assembly");

    private final String language;

    ProgrammingLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
