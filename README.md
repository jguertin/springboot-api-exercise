# Spring Boot Item Surge Pricing Exercise

This is a sample spring boot application that provides the following service functions:
- List items
- Read a single item
- Place an order for a single item
- List orders

When a single item is viewed 10 times within an hour, its price will increase by 10% (rounded to the nearest dollar). A
view is counted as each time it is returned by a call to the item list or read endpoints. Placing an order is not
counted as a view.

## Design Decisions

### Surge Pricing

To handle surge pricing, I implemented a price adjustment interface and implemented a concrete instance for surge
pricing.  This has three configurable options: percentage, window duration and a frequency at which its triggered. When
the system runs an instance is built with 10%, a 1 hour window and a triggering frequency of 10 views.

This surge pricing adjuster is injected into the Item and Order controllers and leveraged to obtain the adjusted price
for a single item.  The implementation allows for the use of different types of adjusters in future enhancements and is
independently testable.

The internal implementation of the pricing adjuster maintains a cache of when each view occurred for each item.  This
cache is updated on each access to ensure the count is accurate.

### Data Format and Handling

Request and response bodies are JSON format serialized to and from DTO objects.  JSON was selected for 2 primary
reasons: It is fully supported by Spring Boot out of the box and it is a widely used and convenient format.

On the server side, DTO objects are used for serialization in and out of the controller layer.  These are transformed
to JPA entities when used through the repository layer.  This is a bit of a duplication, but I find it to be useful
distinction as the API representation of items does not always reflect the database representation.

Often, I would consider implementing services between the controllers and repositories but didn't do so in this case as
I felt that the simplicity of the application did not necessitate it.  In this case it would have just been unnecessary
complexity.

### Authentication

For simplicity, I chose to implement BASIC authentication for this project.  For a more complex project, I would
consider using oauth.  However, this would have required expanded scope to generate and validate tokens.

## System Requirements
Java 11 is required on the path when running from the command line.  Commands in this document assume that you are
running a Mac or Linux compatible shell, but there is also a `gradlew.bat` file available for Windows users.

Gradle is not explicitly required, as the gradle wrapper can be used, which will download its own distribution as
needed.

## Build the project
To compile the project and execute the tests, simply run this in the project folder: `./gradlew build`

This will create distributable tar and zip files in `build/distributions` as well as a test report that can be viewed in
a browser at `build/reports/tests/test/index.html`

This project has a mix of unit and integration tests. Since the project is relatively small the build runs quickly, so
there is no value in running the integration and unit tests separately at this time.

## Run and utilize the project
To run the application as a service on port 8080, run: `./gradlew bootRun`

In order to interact with the API, you can use a tool like [Postman](https://www.getpostman.com/) or
[curl](https://curl.haxx.se/).  Samples provided below are with curl.

### List Items (GET /items)

List all available items in the system: `curl http://localhost:8080/items`

The response will contain JSON similar to:

```[{"id":"870ae15e-526d-4bb4-8d7f-e531b7a82597","name":"iPhone 7","description":"Old iPhone","price":99},{"id":"3039d05a-2a8b-4264-8771-998a1f518468","name":"iPhone 11","description":"New iPhone","price":1099},{"id":"8628b751-20b2-41dc-abdb-451d1a4250d4","name":"Pixel 2","description":"Google's Phone","price":799}]```

Each item in the response has the following corresponding fields in the database:
- id: A unique surrogate key, represented with a UUID.
- name: The name of the item, represented with as text.
- description: A description of the item, represented as text.
- price: The standard price of the item, represented as an integer.
- quantity: The number of units available, represented as an integer.  This value is hidden within the API. Items with
a non-positive value will be excluded from list results.

### Read a Single Item (GET /items/<id>)

Given an id, you may also read just a single item from the API: `curl http://localhost:8080/items/870ae15e-526d-4bb4-8d7f-e531b7a82597`

As long as the id is valid, you will get a JSON response similar to:

```{"id":"870ae15e-526d-4bb4-8d7f-e531b7a82597","name":"iPhone 7","description":"Old iPhone","price":99}```

### Create an Order (POST /orders)

Given an item id, you can create an order for an item.  In this case a POST is required with the item id specified in
the body.  For the order endpoints, you must provide basic authentication with a valid user. The system is preloaded
with a single user with the credentials `user:password`.  A command similar to following can be used to create an order:

```curl -H 'Content-Type: application/json' -d '{"itemId":"870ae15e-526d-4bb4-8d7f-e531b7a82597"}' --user user:password http://localhost:8080/orders```

If this POST completes successfully, the quantity of the specified item will be decremented and a new order will be
created and returned similar to the following:

```{"id":"e837d405-df24-4f0f-a46d-82cfdc0d8e0f","itemId":"870ae15e-526d-4bb4-8d7f-e531b7a82597","username":"user","price":99}```

An order is stored with the following information in the database:
- id: A unique surrogate key, represented with a UUID.
- itemId: The identifier of the item for the order, represented with a UUID.
- username: The username of the user that created the order, represented as text.
- price: The item price applied to the order. This is an adjusted price, which will be 10% above the item price if it
has been viewed 10 times within the hour preceding order creation.

### List Orders (POST /orders)

Just for the convenience to view created orders, an order list endpoint is available that will return all orders in the
system. Like the create endpoint, this is also protected by basic authentication: `curl --user user:password http://localhost:8080/orders`

A sample response would be as follows:

```[{"id":"e837d405-df24-4f0f-a46d-82cfdc0d8e0f","itemId":"870ae15e-526d-4bb4-8d7f-e531b7a82597","username":"user","price":99}]```
