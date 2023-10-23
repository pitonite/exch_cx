package io.github.pitonite.exch_cx.utils

// from https://www.baeldung.com/kotlin/convert-string-enum

inline fun <reified T : Enum<T>> enumByNameIgnoreCase(input: String, default: T? = null): T? {
  return enumValues<T>().firstOrNull { it.name.equals(input, true) } ?: default
}
