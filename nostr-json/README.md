# nostr-json

We design a basic and naive framework for parsing json strings, marshalling any json string to a POJO and unmarshalling POJOs to their json representation. 
We introduce here the concepts of:
 - json values, i.e. number, boolean, string, object and array.
 - and json expressions

## json values and types
- A string value, represented by a text inside double quotes, e.g. `"this is a text"`, is an element of the **JsonStringType**
- A number value can represent any number, e.g. `1`, `2.5` are all elements of **JsonNumberType**, 
- A boolean value with possible values `true` or `false` ( **JsonBooleanType**).
- An array (**JsonArrayType**) is a list of comma-separated **JsonValue**s of arbitrary **JsonTypes**, e.g. `[1,2.3,"hello",["a"], true]`
- An object (**JsonObjectType**) is a list of comma-separated **JsonExpressions** (see below) 

## json expressions
A Json expressions is a pair composed of a variable name and their corresponding **JsonValue**. We use the following syntax to represent an expression: `"<variable name>":<json value>.`

 As an example: `"a":4` represents an expression defining the variable `"a"` (always with quotes) of type number and with the value of `4`.

NOTE: The expression pair `(variable, value)` is also defined as a **JsonValue**.

## HOWTO use the nostr-json API 

### Creating a json POJO

Here I show you how you can create the json object representation of the json string `{"name":"Eric"}`

1. We instantiate a **JsonStringValue**:

    `JsonValue value = new JsonStringValue("Eric");`

2. Then we create the expression object

    `JsonExpression<JsonType> nameExpr = JsonExpression.builder().variable("name").jsonValue(value).build();`

or 

    JsonValue nameExpr = JsonExpression.builder().variable("name").jsonValue(value).build();

or simply use the constructor:

    JsonValue nameExpr = new JsonExpression("name", value);

3: We define the **JsonObjectValue**, as a list of json expressions (`valueList`)

    JsonValueList valueList = new JsonValueList();
    valueList.add(nameExpr);
    JsonObjectValue obj = new JsonObjectValue(valueList);

Alternatively, given a json string, you can use the corresponding unmarshall class as illustrated here below. 

    JsonArrayValue jsonArr = new JsonObjectUnmarshaller("[2,\"a\",[1,2,\"bx\"],\"3\",9]").unmarshall();

Similarly, the classes **JsonArrayUnmarshaller**, **JsonBooleanUnmarshaller**, **JsonExpressionUnmarshaller**, **JsonNumberUnmarshaller**, **JsonStringUnmarshaller** will be used for unmarshalling any json string representations of arrays, booleans, expressions, numbers, and strings.

You reverse the above operation by invoking the `toString()` method on any jsonValue class, e.g. `JsonNumberValue.toString()`, to unmarshall the jsonValue object. The method invokes the `JsonNumberMarshaller(JsonNumberValue arg).marshall()` on the current **JsonNumberValue** object (`this`).
