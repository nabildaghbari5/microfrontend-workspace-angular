package com.talentcloud.auth.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("users")
public class User implements Persistable<UUID> {
    @Id
    @Column("id")
    private UUID id;

    @Column("userid")
    private String userId;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    @Column("role")
    private Role role;

    @Column("keycloak_id")
    private String keycloakId;

    @CreatedDate
    @Column("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setId(UUID id) {
        this.id = id;
        this.isNew = false;
    }

    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}