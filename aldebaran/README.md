Aldebaran Networking Libraries
==============================

Aldebaran are automatic java deserialization and type instantiation libraries for the `DefaultJSONObject` pattern. In particular, Aldebaran supports simple annotations on plain old java objects (*POJO*). By design, therefore, the deserialization details do not interfere with the usage of the objects in any desired manner.

All datatype desiring deserialization must have an appropriate `Type` annotation in order to be automatically deserialized. In addition, data types which naturally represent the result of an API call may implement the `IReturnable` interface to specify their endpoint. The deserializer, however, is robust and can deserialize json from any source to any matching type.

Build instructions
==================

Make sure project.properties contains an android "target" directive. Make sure local.properties contains an sdk.jar entry (like an android library project).

Optionally include junit config information in the local.properties file (see build.xml for required directives).

Usage
=====

The following examples provide a brief overview of the features of the Aldebaran library. For more in-depth documentation please see the javadoc blocks included in the source. 

*In the json examples included below, only the contents of the root `data` element of the DefaultJSONObject are included.*

Simple Object Deserialization
-----------------------------

In it's most basic usage, Aldebaran library transforms a json object into a POJO.

Consider this simplified json representation of a user profile.

```json
{
    "full_name": "Dr. Coccktor",
    "age": 111,
    "adornment": "Gold Chain"
}
```

A suitably tagged POJO would look like

```java
@Type(
    version = 1,
    type = Type.JSONDataType.OBJECT
)
class Profile {
    @Field(
        type = Field.Type.TEXT,
        constraint = Field.Constraint.NOT_NULL,
        json_name = "full_name"
    )
    String name;
    @Field(
        type = Field.Type.NUMBER,
        constrain = Field.Constraint.NOT_NULL
    )
    int age;
    @Field(
        type = Field.Type.TEXT
    )
    String adornment;
}
```

The initial `@Type` annotation declares that this is version 1 of the object declaration. If the fields are changed, then this number should be increased to indicate to the caching engine that it may need to flush the cache. The `@Type.type` field indicates that this represents a single object at the base
of the `data` element, rather than the default `OBJECT_ARRAY` type.

The name field is annotated as a required text field. Furthermore, while the json representation uses the element name `full_name`, the class uses the element name `name`. This mapping is declared using the `json_name` annotation. In general, direct mappings (such as the age and adornment) field do not require the `json_name` annotation to be explicitly set.

The age field is annotated as a numeric type. Final coercion to `int` will occur the an instance of the class is instantiated. By declaring a NUMBER type, however, the caching engine gets a hint as to how it may store the field.

The adornment field does not specify a constraint. As such, if it were not present in the json data, the deserialize would set its value to `null`.

Complex Object Deserialization
------------------------------

For a more complex example, consider the following json snippet.

```json
{
	"developers": [
		{
			"developer_name": "Saguaro Digital",
			"id": 208,
			"apps": [
				{
					"name": "The Ultimate Strip Club List",
					"rating": 4.5
				},
				{
					"name": "Femjoy",
					"rating": 4.9
				}
			]
		},
		{
			"developer_name": "Some Dick Posting Spam (Banned)",
			"id": 123,
			"apps": []
		}
	],
	"some_other_api_data": "..."
}
```

This will require two classes to properly deserialize. First, the inner `Application` class.

```java
@Type(
	version = 1,
	type = Type.JSONDataType.OBJECT
)
class Application {
	@Field(
		type = Field.Type.TEXT,
		constrain = Field.Constraint.NOT_NULL
	)
	String name;
	
	
	@Field(
		type = Field.Type.NUMBER,
		constrain = Field.Constraint.NOT_NULL
	)
	double rating;
}
```

The outer Developer class looks like

```java
@Type(
	version = 1,
	base_name = "developers"
)
class Developer {
	@Field(
		type = Field.Type.TEXT,
		constrain = Field.Constraint.NOT_NULL
	)
	String name;
	
	
	@Field(
		type = Field.Type.NUMBER,
		constrain = Field.Constraint.NOT_NULL,
		json_name = "developer_name"
	)
	int id;
	
	
	@Field(
		type = Field.Type.LIST
	)
	List<Application> apps;
}
```

The annotations on individual fields perform similarly to those in the previous example. Of interest in this example, however, we represent the complex construction of a list of sub-objects by using a `LIST` type. The list could be of the basic object type `Integer`, `String`, `Double` or (as in this case) a specific object which itself has a `@Type` tag.

The Developer object also includes a `base_name` annotation. This indicates to the deserialize that the data lives not in the root of the `data` element, but rather on a sub-element. In general, this represents somewhat bad DefaultJSONObject design. Unfortunately, any number of existing DJO APIs send back an object of the form

```json
{
	"data": {
		"some_key": {
			"actual_data": "..."
		}
	}
}
```

By setting the `base_name` annotation, such DJOs can be cleanly deserialized without requiring an extra level of wrapper objects. As `base_name` represents a special case, it is not general and cannot be applied to multiple levels.

If each developer had exactly one (or zero-to-one) Application objects, the declaration could use a `Field.Type.OBJECT` type on a declared field of `Application apps;`.

API Endpoints
-------------

Thus far the examples have examined just the deserialization annotations. These objects therefore leave up to the application logic the task of obtaining the underlying data, perhaps from a configuration file. Most DJOs, however are requested from a web based API. To simplify this task, any `@Type` tagged object may implement the `IReturnable` interface to specify their API endpoint and caching preferences.

Back to our original example, implementing the `IReturnable` interface would produce

```java
@Type(
	version = 1,
	type = Type.JSONDataType.OBJECT
)
class Profile implements IReturnable {
	@Field(
		type = Field.Type.TEXT,
		constraint = Field.Constraint.NOT_NULL,
		json_name = "full_name"
	)
	String name;
	
	
	@Field(
		type = Field.Type.NUMBER,
		constrain = Field.Constraint.NOT_NULL
	)
	int age;
	
	
	@Field(
		type = Field.Type.TEXT
	)
	String adornment;
	
	@Override
	public IParser<? extends IReturnable> getParser() {
		return new AutoParser<Profile>();
	}
	
	@Override
	public String getUri(Map<String, String> args) {
		return "https://url.com/profile";
	}
	
	@Override
	public IReturnableCache<? extends IReturnable> getCache() {
		return new EmptyCache<Profile>();
	}
}
```

These three methods indicate that the Profile type uses the AutoParser, which is the appropriate parser for most cases. In the event that the data is very irregular and requires special parsing, implementations can create their own parsers by implementing the IParser interface.

The `getUri` method returns a string representation of the URL which the fetch engine must hit to fetch the underlying data. The args parameter is a list of key-value pairs which may be optionally inspected to modify this URL (for instance, to add session information, parameters, etc).

The EmptyCache is the most basic caching engine, and doesn't actually save any data. As more caching engines are added, they will be documented here.

In the second example, only the Developer object would need to implement IReturnable, as the the Application object does not represent an individual API endpoint. In the case that Application objects could be fetched directly as well as encapsulated in Developer objects, then both could implement the IReturnable interface.