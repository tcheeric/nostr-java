# nostr-json

We design a basic and naive framework for parsing json strings, marshalling any json string to a POJO and unmarshalling POJOs to their json representation. 
We introduce here the concepts of:
 - json values, i.e. number, boolean, string, object and array.
 - and json expressions

## values and types
We define:
- A string value (`StringValue`), represented by a text inside double quotes, e.g. `"this is a text"`, is an element of the `Type.STRING`
- A number value (`NumberValue`) can represent any number, e.g. `1`, `2.5` are all elements of `Type.NUMBER`, 
- A boolean value (`BooleanValue`) with possible values `true` or `false` (`Type.BOOLEAN`).
- An array (`ArrayValue`) is a list of comma-separated `Value`s of arbitrary `Type`s, e.g. `[1,2.3,"hello",["a"], true]`
- An expression (`ExpressionValue`), a pair composed of a variable name and their corresponding Value, e.g. `"name":"Nostr"`. 
- An object (`ObjectValue`) is used for grouping expressions as a comma-separated list, e.g. `{"a":4, "b":[true]}` 


## expressions
An expressions is a pair composed of a variable name and their corresponding Value (as above). We use the following syntax to represent an expression: `"<variable name>":<json value>.`

As an example: `"a":4` represents an expression defining the variable name `"a"` (always with quotes) of type number and with the value of `4`.

## HOWTO use the nostr-json API 

### Creating a json POJO

Here I show you how you can create the json object representation of the json string `{"name":"Eric"}`

1. We instantiate a `StringValue`:
    ```java
    IValue value = new StringValue("Eric");
    //StringValue value = new StringValue("Eric")
    ```
2. Then we create the expression object
    ```java
    IValue nameExpr = new ExpressionValue("name", value);
    //ExpressionValue nameExpr = new ExpressionValue("name", value);
    ```
3. We define the `ObjectValue`, as a list of json expressions (`valueList`)
    ```java
    List<IValue> valueList = new ArrayList<>();
    //List<ExpressionValue> valueList = new ArrayList<>();
    valueList.add(nameExpr);
    ObjectValue obj = new ObjectValue(valueList);
    ```

Alternatively, given a json string, you can use the corresponding unmarshall class as illustrated here below. 

    ```java
    ArrayValue jsonArr = new ArrayUnmarshaller("[2,\"a\",[1,2,\"bx\"],\"3\",9]").unmarshall();
    ```

Similarly, the classes `ObjectUnmarshaller`, `BooleanUnmarshaller`, `ExpressionUnmarshaller`, `NumberUnmarshaller`, `StringUnmarshaller` will be used for unmarshalling any json string representations of arrays, booleans, expressions, numbers, and strings.

You reverse the above operation by invoking the `toString()` method on any subclass of `IValue`, e.g. `NumberValue.toString()`. The method will in turn invoke the `NumberMarshaller(NumberValue arg).marshall()` on the current `NumberValue` object (`this`).