package com.mangakousei.mangakousei_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Value("${app.websocket.relay-host}")
    private String relayHost;

    @Value("${app.websocket.relay-port}")
    private int relayPort;

    @Value("${app.websocket.relay-username}")
    private String relayUsername;

    @Value("${app.websocket.relay-password}")
    private String relayPassword;

    @Value("${app.websocket.use-broker-relay:true}")
    private boolean useBrokerRelay;

    @Value("${app.websocket.allowed-origin:http://localhost:5173}")
    private String allowedOrigin;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigin)
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new PrincipalHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");

        if (useBrokerRelay) {
            registry.enableStompBrokerRelay("/topic", "/queue")
                    .setRelayHost(relayHost)
                    .setRelayPort(relayPort)
                    .setClientLogin(relayUsername)
                    .setClientPasscode(relayPassword)
                    .setSystemLogin(relayUsername)
                    .setSystemPasscode(relayPassword)
                    .setSystemHeartbeatSendInterval(10000)
                    .setSystemHeartbeatReceiveInterval(10000);
        } else {
            registry.enableSimpleBroker("/topic", "/queue");
        }
    }
}