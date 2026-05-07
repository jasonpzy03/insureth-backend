rootProject.name = "insureth-backend"

include(
    "auth:auth-ws",
    "auth:auth-service",
    "auth:auth-domain",
    "auth:auth-model"
)

include(
    "insurance:insurance-ws",
    "insurance:insurance-service",
    "insurance:insurance-domain",
    "insurance:insurance-model"
)

include(
    "notification:notification-ws",
    "notification:notification-service",
    "notification:notification-domain",
    "notification:notification-model"
)