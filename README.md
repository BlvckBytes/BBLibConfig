# BBLibConfig

A configuration file object mapper with templating expression features.

## Table Of Contents

* [Variable Substitution](#variable-substitution)
* [Lookup Tables](#lookup-tables)
* [Dynamic Content](#dynamic-content)
  * [IF](#if)
  * [EQUALS](#equals)
  * [LUT_LOOKUP](#lut_lookup)
  * [OR](#or)

## Variable Substitution

Whenever the contents of a variable need to be substituted into a specific location in
your *YAML* string, you have to enclose it's name by curly-brackets twice (this will reduce
ambiguity and thus malintepretations): ``{{ my_variable }}``. The amount of padding spaces
does not matter and only serves for readability, so you might as well use: ``{{my_variable}}``.

## Lookup Tables

Lookup tables (LUT for short) are named tables of key-value pairs which are always located in
the `lut` section of a configuration file. These tables mainly act as translation between
variable values and user displayable information as well as to deduplicate reused values.

```yaml
lut:

  # //////////////// Head textures ////////////////

  textures:
    ARROW_UP: '...'
    ARROW_UP_RED: '...'
    ARROW_DOWN: '...'
    ARROW_DOWN_RED: '...'
```

They can be accessed directly within a variable expression like this: `{{textures["ARROW_UP"]}}`, but
also through the use of variable values: `{{textures[my_variable]}}`. If you need a fallback value we
advise you to check out the lookup table specific operator.

## Dynamic Content

In order to decide on displayed content at runtime, the user has to express all
possible pathways as well as their results in an exact manner. In order to achieve
this, we offer the ability to express these pathways as a tree of operators.

### IF

Evaluates pathway `positive` if `bool` equals:
* String: "true", "yes", "1"
* Number: > 0

and jumps to pathway `negative` in *all other cases*.

```yaml
$evaluate:
  operation: IF
  bool: 'true'
  positive: 'Positive case'
  negative: 'Negative case'
```

### EQUALS

Evaluates pathway `positive` if `valueA` equals `valueB` in it's content, no matter it's type and
jumps to pathway `negative` when mismatching.

```yaml
$evaluate:
  operation: EQUALS
  valueA: 5
  valueB: 4
  # Optional, defaults to true (whether to trim strings before comparison)
  trim: true
  # Optional, defaults to false (whether to ignore casing when comparing strings)
  ignoreCasing: false
  # Optional, defaults to true
  positive: 'These numbers are not equal'
  # Optional, defaults to false
  negative: 'So this pathway will be entered'
```

### LUT_LOOKUP

Looks up the key `lutKey` within the lookup table `lutName` and provides a `defaultValue` as a
fallback if that key or the table could not be located.

```yaml
$evaluate:
  operation: LUT_LOOKUP
  lutName: 'my_lut'
  lutKey: 'my_key'
  defaultValue: 'Fallback value'
```
### OR

Evaluates pathway `positive` if either `boolA` or `boolB` is truthy and jumps to pathway `negative` in all other cases.

```yaml
$evaluate:
  operation: OR
  boolA: true
  boolB: false
  # Optional, defaults to true
  positive: 'One of the two inputs is truthy'
  # Optional, defaults to false
  negative: 'So this pathway will not be entered'
```
