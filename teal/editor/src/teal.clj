(ns editor.teal
  (:require [dynamo.graph :as g]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def grammar
  ;; See https://github.com/teal-language/vscode-teal/blob/master/syntaxes/teal.tmLanguage.json
  {:name "Teal"
   :scope-name "source.teal"
   :indent {:begin #"((^\s*|(\b(local|global)\b\s*)|=\s*)\b(function|macroexp)\b((?!\b(end)\b).)*$)|(((\b(else|then|do|repeat|record|enum)\b((?!\b(end|until)\b).)*)|(\{\s*))$)"
            :end   #"^\s*((\b(elseif|else|end|until)\b)|(\})|(\)))"}
   :line-comment "--"
   :auto-insert {:characters {\" \"
                              \' \'
                              \[ \]
                              \( \)
                              \{ \}
                              \< \>}
                 :close-characters #{\" \' \] \) \} \>}
                 :exclude-scopes    #{"string.quoted.single.teal"
                                      "string.quoted.double.teal"
                                      "string.multiline.teal"
                                      "constant.character.escape.teal"
                                      "constant.character.escape.byte.teal"
                                      "constant.character.escape.unicode.teal"
                                      "comment.block.teal"
                                      "comment.teal"}
                 :open-scopes {\' "punctuation.definition.string.begin.teal"
                               \" "punctuation.definition.string.begin.teal"
                               \[ "punctuation.definition.string.begin.teal"
                               \< "type.arguments.teal"}
                 :close-scopes {\' "punctuation.definition.string.end.teal"
                                \" "punctuation.definition.string.end.teal"
                                \] "punctuation.definition.string.end.teal"
                                \> "type.arguments.teal"}}
   :completion-trigger-characters #{"."}
   :ignored-completion-trigger-characters #{"{" "," ":" "<" ">"}
   :patterns [
              ;; Pragmas
              {:begin #"--#pragma"
               :begin-captures {0 {:name "keyword.control.directive.pragma.teal"}}
               :end #".*$"
               :end-captures {0 {:name "entity.other.attribute-name.pragma.preprocessor.teal"}}}

              ;; Long comments
              {:begin #"--\[(=*)\["
               :begin-captures {0 {:name "punctuation.definition.comment.begin.teal"}}
               :end #"\]\1\]"
               :end-captures {0 {:name "punctuation.definition.comment.end.teal"}}
               :name "comment.block.teal"}

              ;; Short comments
              {:match #"--.*$"
               :name "comment.teal"}

              ;; Constants
              {:match #"\b(nil|true|false)\b"
               :name "constant.language.teal"}

              ;; Numbers - hexadecimal integer
              {:match #"(?<![\w\d.])0[xX][0-9A-Fa-f]+(?![pPeE.0-9])"
               :name "constant.numeric.integer.hexadecimal.teal"}

              ;; Numbers - hexadecimal float
              {:match #"(?<![\w\d.])0[xX][0-9A-Fa-f]+(\.[0-9A-Fa-f]+)?([eE]-?\d*)?([pP][-+]\d+)?"
               :name "constant.numeric.float.hexadecimal.teal"}

              ;; Numbers - decimal integer
              {:match #"(?<![\w\d.])\d+(?![pPeE.0-9])"
               :name "constant.numeric.integer.teal"}

              ;; Numbers - decimal float
              {:match #"(?<![\w\d.])\d+(\.\d+)?([eE]-?\d*)?"
               :name "constant.numeric.float.teal"}

              ;; Single quoted strings
              {:begin #"'"
               :begin-captures {0 {:name "punctuation.definition.string.begin.teal"}}
               :end #"'"
               :end-captures {0 {:name "punctuation.definition.string.end.teal"}}
               :name "string.quoted.single.teal"
               :patterns [{:match #"\\[abfnrtvz\\\"'\n]"
                           :name "constant.character.escape.teal"}
                          {:match #"\\\d{1,3}"
                           :name "constant.character.escape.byte.teal"}
                          {:match #"\\x[0-9A-Fa-f][0-9A-Fa-f]"
                           :name "constant.character.escape.byte.teal"}
                          {:match #"\\u\{[0-9A-Fa-f]+\}"
                           :name "constant.character.escape.unicode.teal"}
                          {:match #"\\."
                           :name "invalid.illegal.character.escape.teal"}]}

              ;; Double quoted strings
              {:begin #"\""
               :begin-captures {0 {:name "punctuation.definition.string.begin.teal"}}
               :end #"\""
               :end-captures {0 {:name "punctuation.definition.string.end.teal"}}
               :name "string.quoted.double.teal"
               :patterns [{:match #"\\[abfnrtvz\\\"'\n]"
                           :name "constant.character.escape.teal"}
                          {:match #"\\\d{1,3}"
                           :name "constant.character.escape.byte.teal"}
                          {:match #"\\x[0-9A-Fa-f][0-9A-Fa-f]"
                           :name "constant.character.escape.byte.teal"}
                          {:match #"\\u\{[0-9A-Fa-f]+\}"
                           :name "constant.character.escape.unicode.teal"}
                          {:match #"\\."
                           :name "invalid.illegal.character.escape.teal"}]}

              ;; Long strings
              {:begin #"\[(=*)\["
               :begin-captures {0 {:name "punctuation.definition.string.begin.teal"}}
               :end #"\]\1\]"
               :end-captures {0 {:name "punctuation.definition.string.end.teal"}}
               :name "string.multiline.teal"}

              ;; Attributes
              {:match #"<\s*(const|close|total)\s*>"
               :name "storage.modifier.teal"}

              ;; Function definitions
              {:begin #"\b(function)\s+([a-zA-Z_.:]+[.:])?([a-zA-Z_]\w*)\s*(?:<([a-zA-Z_][a-zA-Z0-9_, ]*)\s*>)?\s*(\()"
               :begin-captures {1 {:name "keyword.declaration.function.teal"}
                                2 {:name "entity.name.function.scope.teal"}
                                3 {:name "entity.name.function.teal"}
                                4 {:name "support.type.teal"}
                                5 {:name "punctuation.definition.parameters.begin.teal"}}
               :end #"(\))(?:\s*:\s*([^=\n]*))?"
               :end-captures {1 {:name "punctuation.definition.parameters.end.teal"}
                              2 {:name "support.type.teal"}}
               :name "meta.function.teal"
               :patterns [{:match #"[a-zA-Z_]\w*\s*(?=:)"
                           :name "variable.parameter.function.teal"}
                          {:match #":"
                           :name "punctuation.separator.type.teal"}
                          {:match #"[a-zA-Z_]\w*"
                           :name "support.type.teal"}
                          {:match #","
                           :name "punctuation.separator.parameter.teal"}]}

              ;; Anonymous function (inline function expressions)
              {:begin #"\b(function)\s*(?:<([a-zA-Z_][a-zA-Z0-9_, ]*)\s*>)?\s*(\()"
               :begin-captures {1 {:name "keyword.declaration.function.teal"}
                                2 {:name "support.type.teal"}
                                3 {:name "punctuation.definition.parameters.begin.teal"}}
               :end #"(\))(?:\s*:\s*([^=\n]*))?"
               :end-captures {1 {:name "punctuation.definition.parameters.end.teal"}
                              2 {:name "support.type.teal"}}
               :name "meta.function.anonymous.teal"
               :patterns [{:match #"[a-zA-Z_]\w*\s*(?=:)"
                           :name "variable.parameter.function.teal"}
                          {:match #":"
                           :name "punctuation.separator.type.teal"}
                          {:match #"[a-zA-Z_]\w*"
                           :name "support.type.teal"}
                          {:match #","
                           :name "punctuation.separator.parameter.teal"}]}

              ;; Record and interface definitions
              {:begin #"\b(record|interface)\s+([a-zA-Z_]\w*)(?:\s*<([a-zA-Z_][a-zA-Z0-9_, ]*)\s*>)?"
               :begin-captures {1 {:name "storage.type.record.teal"}
                                2 {:name "entity.name.type.teal"}
                                3 {:name "support.type.teal"}}
               :end #"\b(end)\b"
               :end-captures {1 {:name "storage.type.record.teal"}}
               :name "meta.record.teal"
               :patterns [{:match #"\b(is)\b"
                           :name "keyword.other.teal"}
                          {:match #"\b(where)\b"
                           :name "keyword.control.teal"}
                          {:match #"^\s*\b(userdata)\b"
                           :name "storage.type.userdata.teal"}
                          {:match #"^\s*\b(metamethod)\b"
                           :name "storage.type.metamethod.teal"}
                          {:match #"([a-zA-Z_]\w*)\s*:"
                           :captures {1 {:name "variable.other.teal"}}}
                          {:match #"[a-zA-Z_]\w*"
                           :name "support.type.teal"}]}

              ;; Enum definitions
              {:begin #"\b(enum)\b"
               :begin-captures {1 {:name "storage.type.enum.teal"}}
               :end #"\b(end)\b"
               :end-captures {1 {:name "storage.type.enum.teal"}}
               :name "meta.enum.teal"
               :patterns [{:match #"[a-zA-Z_]\w*"
                           :name "support.type.teal"}]}

              ;; Type declarations
              {:begin #"\b(type)\s+([a-zA-Z_]\w*)"
               :begin-captures {1 {:name "keyword.declaration.type.teal"}
                                2 {:name "support.type.teal"}}
               :end #"$"
               :name "meta.type.declaration.teal"
               :patterns [{:match #"="
                           :name "keyword.operator.assignment.teal"}
                          {:match #"[a-zA-Z_]\w*"
                           :name "support.type.teal"}]}

              ;; Control flow keywords
              {:match #"\b(if|then|elseif|else|end|for|while|repeat|until|do|break|goto|return)\b"
               :name "keyword.control.teal"}

              ;; Storage modifiers
              {:match #"\b(local|global)\b"
               :name "storage.modifier.teal"}

              ;; Logical operators
              {:match #"\b(and|or|not)\b"
               :name "keyword.operator.logical.teal"}

              ;; Teal-specific operators
              {:match #"\b(as|is|in)\b"
               :name "keyword.other.teal"}

              ;; Arithmetic and comparison operators
              {:match #"\+|-|%|#|\*|\/|\^|==?|~=|<=?|>=?|(?<!\.)\.{2}(?!\.)"
               :name "keyword.operator.teal"}

              ;; Built-in functions
              {:match #"(?<![^.]\.|:)\b(assert|collectgarbage|dofile|error|getfenv|getmetatable|ipairs|loadfile|loadstring|module|next|pairs|pcall|print|rawequal|rawget|rawset|require|select|setfenv|setmetatable|tonumber|tostring|type|unpack|xpcall)\b(?=\s*(?:[({\"']|\[\[))"
               :name "support.function.teal"}

              ;; Library functions
              {:match #"(?<![^.]\.|:)\b(coroutine\.(create|resume|running|status|wrap|yield)|string\.(byte|char|dump|find|format|gmatch|gsub|len|lower|match|rep|reverse|sub|upper)|table\.(concat|insert|maxn|remove|sort)|math\.(abs|acos|asin|atan2?|ceil|cosh?|deg|exp|floor|fmod|frexp|ldexp|log|log10|max|min|modf|pow|rad|random|randomseed|sinh?|sqrt|tanh?)|io\.(close|flush|input|lines|open|output|popen|read|tmpfile|type|write)|os\.(clock|date|difftime|execute|exit|getenv|remove|rename|setlocale|time|tmpname)|package\.(cpath|loaded|loadlib|path|preload|seeall)|debug\.(debug|[gs]etfenv|[gs]ethook|getinfo|[gs]etlocal|[gs]etmetatable|getregistry|[gs]etupvalue|traceback))\b(?=\s*(?:[({\"']|\[\[))"
               :name "support.function.library.teal"}

              ;; Function calls
              {:match #"\b([A-Za-z_]\w*)\b(?=\s*(?:[({\"']|\[\[))"
               :name "support.function.any-method.teal"}

              ;; Self reference
              {:match #"(?<![^.]\.|:)\b(self)\b"
               :name "variable.language.self.teal"}

              ;; Type annotations
              {:begin #":"
               :end #"(?=\s*[=,;)\]}]|$)"
               :patterns [{:match #"[a-zA-Z_]\w*"
                           :name "support.type.teal"}
                          {:match #"\."
                           :name "punctuation.accessor.teal"}
                          {:begin #"<"
                           :end #">"
                           :name "type.arguments.teal"
                           :patterns [{:match #"[a-zA-Z_]\w*"
                                       :name "support.type.teal"}
                                      {:match #","
                                       :name "punctuation.separator.teal"}]}
                          {:begin #"\{"
                           :end #"\}"
                           :name "support.tabletype.teal"
                           :patterns [{:match #"[a-zA-Z_]\w*"
                                       :name "support.type.teal"}
                                      {:match #":"
                                       :name "punctuation.separator.type.teal"}
                                      {:match #","
                                       :name "punctuation.separator.teal"}]}
                          {:begin #"\bfunction\b"
                           :end #"(?=\s*[=,;)\]}]|$)"
                           :begin-captures {0 {:name "keyword.declaration.function.teal"}}
                           :patterns [{:begin #"\("
                                       :end #"\)"
                                       :patterns [{:match #"[a-zA-Z_]\w*"
                                                   :name "support.type.teal"}
                                                  {:match #","
                                                   :name "punctuation.separator.teal"}]}
                                      {:match #"[a-zA-Z_]\w*"
                                       :name "support.type.teal"}]}]}

              ;; Variable access (field access)
              {:match #"(?<=[^.]\.|:)\b([a-zA-Z_]\w*)"
               :name "variable.other.teal"}

              ;; General identifiers (variables)
              {:match #"\b([a-zA-Z_]\w*)\b"
               :name "variable.other.teal"}]})

(defn load-plugin [workspace]
  (let [language "teal"
        view-opts {:code {:grammar grammar}}]
    (g/transact
      (concat
        (g/update-property workspace :resource-types update "tl" assoc
                           :language language
                           :view-opts view-opts)
        (g/update-property workspace :resource-types-non-editable update "tl" assoc
                           :language language
                           :view-opts view-opts)))))
