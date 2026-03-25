package org.nmcpye.datarun.jpa.errorevent;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// ErrorEvent.java
@Entity
@Table(name = "error_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="occurred_at")
    @Builder.Default
    private Instant occurredAt = Instant.now();
    private String level;
    private Integer status;
    private String path;
    private String method;
    private String username;
    private String clientIp;
    @Column(columnDefinition = "text")
    private String message;
    private String exception;
    @Column(columnDefinition = "text")
    private String stacktrace;
    @Column(name="context_json", columnDefinition = "text")
    private String contextJson;
}
