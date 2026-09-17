#pragma once

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdatomic.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    int32_t code;
} FfiStatus;

#define FFI_STATUS_OK ((FfiStatus){0})
#define FFI_STATUS_NULL_POINTER ((FfiStatus){1})
#define FFI_STATUS_BUFFER_TOO_SMALL ((FfiStatus){2})
#define FFI_STATUS_INVALID_ARG ((FfiStatus){3})
#define FFI_STATUS_CANCELLED ((FfiStatus){4})
#define FFI_STATUS_INTERNAL_ERROR ((FfiStatus){100})

typedef struct {
    uint8_t *ptr;
    uintptr_t len;
    uintptr_t cap;
    uintptr_t align;
} FfiBuf_u8;

typedef struct {
    uint8_t *ptr;
    uintptr_t len;
    uintptr_t cap;
} FfiString;

typedef struct {
    FfiString message;
} FfiError;

typedef struct {
    const uint8_t *ptr;
    uintptr_t len;
} FfiSpan;

typedef const void *RustFutureHandle;
typedef int8_t StreamPollResult;
typedef int32_t WaitResult;
typedef void (*RustFutureContinuationCallback)(uint64_t callback_data, int8_t poll_result);
typedef void (*StreamContinuationCallback)(uint64_t callback_data, StreamPollResult result);

static inline bool boltffi_atomic_u8_cas(uint8_t *state, uint8_t expected, uint8_t desired) {
    return atomic_compare_exchange_strong_explicit((_Atomic uint8_t *)state, &expected, desired, memory_order_acq_rel, memory_order_acquire);
}

static inline uint64_t boltffi_atomic_u64_exchange(uint64_t *slot, uint64_t value) {
    return atomic_exchange_explicit((_Atomic uint64_t *)slot, value, memory_order_acq_rel);
}

static inline bool boltffi_atomic_u64_cas(uint64_t *slot, uint64_t expected, uint64_t desired) {
    return atomic_compare_exchange_strong_explicit((_Atomic uint64_t *)slot, &expected, desired, memory_order_acq_rel, memory_order_acquire);
}

static inline uint64_t boltffi_atomic_u64_load(uint64_t *slot) {
    return atomic_load_explicit((_Atomic uint64_t *)slot, memory_order_acquire);
}

typedef struct {
    uint64_t handle;
    const void *vtable;
} BoltFFICallbackHandle;

void boltffi_free_string(FfiString string);
void boltffi_free_buf(FfiBuf_u8 buf);
FfiBuf_u8 boltffi_buf_from_bytes(const uint8_t *ptr, uintptr_t len);
FfiBuf_u8 boltffi_buf_with_len(uintptr_t len);
FfiStatus boltffi_last_error_message(FfiString *out);
void boltffi_clear_last_error(void);
typedef struct {
    uint64_t start;
    uint64_t old_end;
    uint64_t new_end;
} ___DiffRange;
typedef int32_t ___TSBridgeError;
#define TS_BRIDGE_ERROR_LANGUAGE_ERROR ((___TSBridgeError)0)
#define TS_BRIDGE_ERROR_POISONED_LOCK_ERROR ((___TSBridgeError)1)
#define TS_BRIDGE_ERROR_OTHER_ERROR ((___TSBridgeError)2)
#define TS_BRIDGE_ERROR_TREE_CREATION_ERROR ((___TSBridgeError)3)
typedef int32_t ___TSLang;
#define TS_LANG_ASM ((___TSLang)0)
#define TS_LANG_CPP ((___TSLang)1)
#define TS_LANG_C ((___TSLang)2)
#define TS_LANG_C_SHARP ((___TSLang)3)
#define TS_LANG_OBJ_C ((___TSLang)4)
#define TS_LANG_JAVA ((___TSLang)5)
#define TS_LANG_KOTLIN ((___TSLang)6)
#define TS_LANG_RUST ((___TSLang)7)
#define TS_LANG_ZIG ((___TSLang)8)
#define TS_LANG_GLEAM ((___TSLang)9)
#define TS_LANG_ODIN ((___TSLang)10)
#define TS_LANG_GLSL ((___TSLang)11)
#define TS_LANG_HLSL ((___TSLang)12)
#define TS_LANG_SLANG ((___TSLang)13)
#define TS_LANG_JS ((___TSLang)14)
#define TS_LANG_TS ((___TSLang)15)
#define TS_LANG_TSX ((___TSLang)16)
#define TS_LANG_PHP ((___TSLang)17)
#define TS_LANG_PHPO ((___TSLang)18)
#define TS_LANG_HASKELL ((___TSLang)19)
#define TS_LANG_GO ((___TSLang)20)
#define TS_LANG_RUBY ((___TSLang)21)
#define TS_LANG_PYTHON ((___TSLang)22)
#define TS_LANG_SWIFT ((___TSLang)23)
#define TS_LANG_LUA ((___TSLang)24)
#define TS_LANG_CLOJURE ((___TSLang)25)
#define TS_LANG_R ((___TSLang)26)
#define TS_LANG_ELIXIR ((___TSLang)27)
#define TS_LANG_O_CAML ((___TSLang)28)
#define TS_LANG_O_CAML_I ((___TSLang)29)
#define TS_LANG_O_CAMT_T ((___TSLang)30)
#define TS_LANG_SCALA ((___TSLang)31)
#define TS_LANG_TOML ((___TSLang)32)
#define TS_LANG_C_MAKE ((___TSLang)33)
#define TS_LANG_NIX ((___TSLang)34)
#define TS_LANG_REGEX ((___TSLang)35)
#define TS_LANG_YAML ((___TSLang)36)
#define TS_LANG_JSON ((___TSLang)37)
#define TS_LANG_CSS ((___TSLang)38)
#define TS_LANG_HTML ((___TSLang)39)
#define TS_LANG_MD ((___TSLang)40)
#define TS_LANG_SQL ((___TSLang)41)
void boltffi_release_class_tree_sitter_bridge_ts_bridge(uint64_t handle);
uint64_t boltffi_init_class_tree_sitter_bridge_ts_bridge_new(void);
FfiBuf_u8 boltffi_method_class_tree_sitter_bridge_ts_bridge_set_language(uint64_t receiver, ___TSLang lang);
FfiBuf_u8 boltffi_method_class_tree_sitter_bridge_ts_bridge_get_kinds_for_selected_language(uint64_t receiver, FfiBuf_u8 *return_out);
FfiBuf_u8 boltffi_method_class_tree_sitter_bridge_ts_bridge_set_initial_string(uint64_t receiver, const uint8_t *source_string_ptr, uintptr_t source_string_len);
FfiBuf_u8 boltffi_method_class_tree_sitter_bridge_ts_bridge_get_previous_parse(uint64_t receiver, FfiBuf_u8 *return_out);
FfiBuf_u8 boltffi_method_class_tree_sitter_bridge_ts_bridge_parse_everything(uint64_t receiver, const uint8_t *source_string_ptr, uintptr_t source_string_len, FfiBuf_u8 *return_out);
FfiBuf_u8 boltffi_method_class_tree_sitter_bridge_ts_bridge_parse_changes(uint64_t receiver, const uint8_t *changed_part_ptr, uintptr_t changed_part_len, ___DiffRange diff_range, FfiBuf_u8 *return_out);

#ifdef __cplusplus
}
#endif