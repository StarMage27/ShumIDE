use strum_macros::EnumIter; // 0.17.1

//noinspection ALL
#[derive(uniffi::Enum, PartialEq, Debug, EnumIter, Copy, Clone)]
pub enum TSLang {
    ASM,
    CPP,
    C,
    CSharp,
    ObjC,

    Java,
    Kotlin,

    Rust,
    Zig,
    Gleam,
    Odin,

    GLSL,
    HLSL,
    //WGSL,
    //WGSLB,

    JS,
    TS,
    TSX,
    PHP,
    PHPO,

    Haskell,
    Go,
    Ruby,
    Python,
    Swift,
    Lua,

    Clojure,
    R,
    Elixir,
    OCaml,
    OCamlI,
    OCamtT,
    Scala,

    Toml,
    CMake,
    Nix,
    Regex,
    Yaml,
    Json,
    CSS,
    HTML,
    MD,
    SQL,
}
