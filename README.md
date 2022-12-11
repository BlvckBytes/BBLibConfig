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
  * [COMPARE](#compare)
  * [CONCAT](#concat)
  * [MATH](#math)
  * [DATE_FORMAT](#date_format)
  * [NUMBER_FORMAT](#number_format)

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

All logical operations support the `negate` property which will in essence flip it's `positive` and
`negative` cases internally.

```yaml
$evaluate:
  operation: <...>
  # Optional, defaults to false
  negate: true
```

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

### COMPARE

Evaluates pathway `positive` if `valueA` compares positively to `valueB` in it's content, no matter it's
type and jumps to pathway `negative` when mismatching.

These are all available `mode`s of comparison:

| Mode                  | Mathematical Equivalent |
|-----------------------|-------------------------|
| GREATER_THAN          | A > B                   |
| GREATER_THAN_OR_EQUAL | A ≥ B                   |
| LESS_THAN             | A < B                   |
| LESS_THAN_OR_EQUAL    | A ≤ B                   |

```yaml
$evaluate:
  operation: COMPARE
  valueA: 5
  valueB: 4
  mode: GREATER_THAN
  # Optional, defaults to true
  positive: 'Value a is greater than b'
  # Optional, defaults to false
  negative: 'So this pathway will not be entered'
```

### CONCAT

Concatenates the values `stringA` and `stringB` by interpreting them as strings and joining them
using the `separator` string, if provided. This operation becomes useful as soon as you're trying to
join values transformed by other operations (temporary values, in essence) with other values.

```yaml
$evaluate:
  operation: CONCAT
  stringA: 'Welcome'
  stringB: '{{username}}'
  # Optional, defaults to none
  separator: ' '
```
### MATH

Performs the specified math `mode` on it's two input values `valueA` and `valueB`. If an input
is not a numeric value, the result will default to zero.

These are all available math `mode`s:

| Mode     | Mathematical Equivalent |
|----------|-------------------------|
| PLUS     | A + B                   |
| MINUS    | A - B                   |
| MULTIPLY | A * B                   |
| DIVIDE   | A / B                   |
| POW      | A ^ B                   |

```yaml
$evaluate:
  operation: MATH
  mode: POW
  valueA: 5
  valueB: 4
```

### DATE_FORMAT

Formats the input `date` by applying the specified `format`
([format notation](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)).

```yaml
$evaluate:
  operation: DATE_FORMAT
  date: '{{now}}'
  format: "dd.MM.yyyy '@' HH:mm:ss"
```

### NUMBER_FORMAT

Formats the input `number` by applying all formatting parameters to it and keep non-numeric values untouched.

| Separation Mode | Example         |
|-----------------|-----------------|
| NONE            | 1000000.0       |
| TENS            | 1,0,0,0,0,0,0.0 |
| HUNDREDS        | 1,00,00,00.0    |
| THOUSANDS       | 1,000,000.0     |
| MILLIONS        | 1,000000.0      |


| Rounding Mode | Example                          |
|---------------|----------------------------------|
| NONE          | 100.12                           |
| DOWN          | 100.12 -> 100.0                  |
| UP            | 100.12 -> 101.0                  |
| HALF          | 100.12 -> 100.0, 100.55 -> 101.0 |

```yaml
$evaluate:
  operation: NUMBER_FORMAT
  number: 100000.123
  # Optional, defaults to '.'
  decimalString: '.'
  # Optional, defaults to NONE
  roundingMode: HALF
  # Optional, defaults to 0
  numberOfDecimals: 2
  # Optional, defaults to NONE
  separationMode: HUNDREDS
  # Optional, defaults to ','
  separationString: ','
  # Optional, defaults to 0
  paddingSize: 0
```