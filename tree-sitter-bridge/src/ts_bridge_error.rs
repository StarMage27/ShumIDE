#[derive(Debug, Clone)]
#[boltffi::error]
pub enum TSBridgeError {
    LanguageError,
    PoisonedLockError,
    OtherError,
    TreeCreationError
}

impl std::fmt::Display for TSBridgeError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.to_string())
    }
}

impl std::error::Error for TSBridgeError {
    fn description(&self) -> &str {
        "Error loading Grammar"
    }
}

impl From<tree_sitter::LanguageError> for TSBridgeError {
    fn from(_: tree_sitter::LanguageError) -> Self {
        TSBridgeError::LanguageError
    }
}

impl From<anyhow::Error> for TSBridgeError {
    fn from(_: anyhow::Error) -> Self {
        TSBridgeError::OtherError
    }
}

impl<T> From<std::sync::PoisonError<std::sync::MutexGuard<'_, T>>> for TSBridgeError {
    fn from(_: std::sync::PoisonError<std::sync::MutexGuard<T>>) -> Self {
        TSBridgeError::PoisonedLockError
    }
}
