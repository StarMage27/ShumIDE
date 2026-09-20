mod ts_bridge_error;
mod ts_language;
use std::sync::Mutex;
use tree_sitter::{InputEdit, Language, Parser, Tree};
use crate::ts_bridge_error::TSBridgeError;
use crate::ts_language::TSLang;

pub struct TSBridge {
    parser: Mutex<Parser>,
    previous_tree: Mutex<Option<Tree>>,
    previous_string: Mutex<String>,
    previous_highlights: Mutex<Vec<Highlight>>,
}

#[boltffi::export]
impl TSBridge {
    pub fn new() -> Self {
        TSBridge {
            parser: Mutex::new(Parser::new()),
            previous_tree: Mutex::new(None),
            previous_string: Mutex::new(String::new()),
            previous_highlights: Mutex::new(Vec::new()),
        }
    }

    /// Sets the language and returns vec of kinds as strings
    pub fn set_language(&self, lang: TSLang) -> Result<(), TSBridgeError> {
        let language: Language = match lang {
            TSLang::ASM     => { tree_sitter_asm       ::LANGUAGE                .into() }
            TSLang::CPP     => { tree_sitter_cpp       ::LANGUAGE                .into() }
            TSLang::C       => { tree_sitter_c         ::LANGUAGE                .into() }
            TSLang::CSharp  => { tree_sitter_c_sharp   ::LANGUAGE                .into() }
            TSLang::ObjC    => { tree_sitter_objc      ::LANGUAGE                .into() }

            TSLang::Java    => { tree_sitter_java      ::LANGUAGE                .into() }
            TSLang::Kotlin  => { tree_sitter_kotlin_ng ::LANGUAGE                .into() }

            TSLang::Rust    => { tree_sitter_rust      ::LANGUAGE                .into() }
            TSLang::Zig     => { tree_sitter_zig       ::LANGUAGE                .into() }
            TSLang::Gleam   => { tree_sitter_gleam     ::LANGUAGE                .into() }
            TSLang::Odin    => { tree_sitter_odin      ::LANGUAGE                .into() }

            TSLang::GLSL    => { tree_sitter_glsl      ::LANGUAGE_GLSL           .into() }
            TSLang::HLSL    => { tree_sitter_hlsl      ::LANGUAGE_HLSL           .into() }
            TSLang::SLANG   => { tree_sitter_slang     ::LANGUAGE_SLANG          .into() }

            TSLang::JS      => { tree_sitter_javascript::LANGUAGE                .into() }
            TSLang::TS      => { tree_sitter_typescript::LANGUAGE_TYPESCRIPT     .into() }
            TSLang::TSX     => { tree_sitter_typescript::LANGUAGE_TSX            .into() }
            TSLang::PHP     => { tree_sitter_php       ::LANGUAGE_PHP            .into() }
            TSLang::PHPO    => { tree_sitter_php       ::LANGUAGE_PHP_ONLY       .into() }

            TSLang::Haskell => { tree_sitter_haskell   ::LANGUAGE                .into() }
            TSLang::Go      => { tree_sitter_go        ::LANGUAGE                .into() }
            TSLang::Ruby    => { tree_sitter_ruby      ::LANGUAGE                .into() }
            TSLang::Python  => { tree_sitter_python    ::LANGUAGE                .into() }
            TSLang::Swift   => { tree_sitter_swift     ::LANGUAGE                .into() }
            TSLang::Lua     => { tree_sitter_lua       ::LANGUAGE                .into() }

            TSLang::Clojure => { arborium_clojure      ::language()              .into() }
            TSLang::R       => { tree_sitter_r         ::LANGUAGE                .into() }
            TSLang::Elixir  => { tree_sitter_elixir    ::LANGUAGE                .into() }
            TSLang::OCaml   => { tree_sitter_ocaml     ::LANGUAGE_OCAML          .into() }
            TSLang::OCamlI  => { tree_sitter_ocaml     ::LANGUAGE_OCAML_INTERFACE.into() }
            TSLang::OCamtT  => { tree_sitter_ocaml     ::LANGUAGE_OCAML_TYPE     .into() }
            TSLang::Scala   => { tree_sitter_scala     ::LANGUAGE                .into() }

            TSLang::Toml    => { tree_sitter_toml_ng   ::LANGUAGE                .into() }
            TSLang::CMake   => { tree_sitter_cmake     ::LANGUAGE                .into() }
            TSLang::Nix     => { tree_sitter_nix       ::LANGUAGE                .into() }
            TSLang::Regex   => { tree_sitter_regex     ::LANGUAGE                .into() }
            TSLang::Yaml    => { tree_sitter_yaml      ::LANGUAGE                .into() }
            TSLang::Json    => { tree_sitter_json      ::LANGUAGE                .into() }
            TSLang::CSS     => { tree_sitter_css       ::LANGUAGE                .into() }
            TSLang::HTML    => { tree_sitter_html      ::LANGUAGE                .into() }
            TSLang::MD      => { tree_sitter_md        ::LANGUAGE                .into() }
            TSLang::SQL     => { tree_sitter_sequel    ::LANGUAGE                .into() }
        };

        // self.parser.set_language(&language)?;
        self.parser.lock()?.set_language(&language)?;

        Ok(())
    }

    pub fn get_kinds_for_selected_language(&self) -> Result<Vec<String>, TSBridgeError> {
        let parser = self.parser.lock()?;
        let language = parser.language();
        match language {
            Some(language) => {
                let count = language.node_kind_count();
                let mut table = Vec::with_capacity(count);

                for id in 0..count {
                    if let Some(kind) = language.node_kind_for_id(id as u16) {
                        table.push(kind.to_string());
                    } else {
                        table.push(String::new());
                    }
                }
                Ok(table)
            },
            None => {
                Err(TSBridgeError::LanguageError)
            }
        }
    }

    pub fn set_initial_string(&self, source_string: &str) -> Result<(), TSBridgeError> {
        let mut previous_string = self.previous_string.lock()?;
        previous_string.clear();
        previous_string.push_str(source_string);

        Ok(())
    }

    pub fn get_previous_parse(&self) -> Result<Vec<Highlight>, TSBridgeError> {
        let previous_highlights = self.previous_highlights.lock()?;
        let highlights: Vec<Highlight> = previous_highlights.clone();

        Ok(highlights)
    }

    pub fn parse_everything(&self, source_string: &str) -> Result<Vec<Highlight>, TSBridgeError> {
        let mut parser = self.parser.lock()?;
        let mut previous_string = self.previous_string.lock()?;
        let mut previous_tree = self.previous_tree.lock()?;

        previous_string.clear();
        previous_string.push_str(source_string);

        let tree = parser.parse(previous_string.as_str(), previous_tree.as_ref());

        match tree {
            Some(tree) => {
                let highlights = climb(&tree);
                previous_tree.replace(tree);

                Ok(highlights)
            }
            None => {
                Err(TSBridgeError::TreeCreationError)
            }
        }
    }

    pub fn parse_changes(&self, changed_part: &str, diff_range: DiffRange) -> Result<Vec<Highlight>, TSBridgeError> {
        let mut parser = self.parser.lock()?;
        let mut previous_tree = self.previous_tree.lock()?;
        let mut previous_string = self.previous_string.lock()?;

        let start_byte = diff_range.start as usize;
        let old_end_byte = diff_range.old_end as usize;
        let new_end_byte = diff_range.new_end as usize;

        match previous_tree.as_mut() {
            Some(previous_tree) => {
                let input_edit = InputEdit {
                    start_byte,
                    old_end_byte,
                    new_end_byte,
                    start_position: Default::default(),
                    old_end_position: Default::default(),
                    new_end_position: Default::default(),
                };
                previous_tree.edit(&input_edit);
            }
            None => {}
        }
        previous_string.replace_range(start_byte..old_end_byte, changed_part);

        let tree = parser.parse(previous_string.as_str(), previous_tree.as_ref());
        if tree.is_none() { return Err(TSBridgeError::TreeCreationError); } // Return if tree is None, which is almost never(?)
        let tree = tree.unwrap();

        let highlights = climb(&tree);
        previous_tree.replace(tree.clone());

        Ok(highlights)
    }
}

#[allow(proc_macro_derive_resolution_fallback)]
#[boltffi::data]
#[derive(Clone, Copy)]
pub struct Highlight {
    pub start: u64,
    pub end: u64,
    pub kind: u16,
}

#[boltffi::data]
#[derive(Clone, Copy)]
pub struct DiffRange {
    pub start: u64,
    pub old_end: u64,
    pub new_end: u64,
}

pub fn climb(tree: &Tree) -> Vec<Highlight> {
    let root = tree.root_node();

    let mut highlights: Vec<Highlight> = Vec::new();

    let mut stack = vec![root];
    while let Some(node) = stack.pop() {
        highlights.push(Highlight{
            start: node.start_byte() as u64,
            end: node.end_byte() as u64,
            kind: node.kind_id()
        });

        for i in (0..node.child_count()).rev() {
            if let Some(child) = node.child(i) {
                stack.push(child);
            }
        }
    }

    highlights
}

#[cfg(test)]
mod tests {

use strum::IntoEnumIterator;
    use crate::TSLang;
use crate::TSBridge;
    #[test]
    fn test_all_languages() {
        let bridge = TSBridge::new();

        for lang in TSLang::iter() {
            match bridge.set_language(lang) {
                Ok(_) => println!("Passed: {:?}" , lang),
                Err(e) => panic!("Failed: {:?}. Error: {:?}", lang, e),
            }
        }
    }
}
