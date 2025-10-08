use std::error::Error;
use std::fmt::{Display, Formatter};
use std::sync::{MutexGuard, PoisonError};
use tree_sitter::LanguageError;

#[derive(uniffi::Error, Debug)]
pub enum TSBridgeError {
    LanguageError {
        error_message: String
    },
    PoisonedLockError {
        error_message: String,
    },
    OtherError {
        error_message: String,
    },
    TreeCreationError
}

impl Display for TSBridgeError {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.to_string())
    }
}

impl Error for TSBridgeError {
    fn description(&self) -> &str {
        "Error loading Grammar"
    }
}

impl From<LanguageError> for TSBridgeError {
    fn from(err: LanguageError) -> Self {
        TSBridgeError::LanguageError { error_message: err.to_string() }
    }
}

impl From<anyhow::Error> for TSBridgeError {
    fn from(err: anyhow::Error) -> Self {
        TSBridgeError::OtherError { error_message: err.to_string() }
    }
}

impl<T> From<PoisonError<MutexGuard<'_, T>>> for TSBridgeError {
    fn from(value: PoisonError<MutexGuard<'_, T>>) -> Self {
        TSBridgeError::PoisonedLockError { error_message: value.to_string() }
    }
}
