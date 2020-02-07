# Money transfer API

## Build & run

```
./gradlew run
```


## Implementation notes

Platform - Java 11; Storage - in-memory H2 database; HTTP server - Jetty; Using SparkJava for routing 
(I did not want to spend time on writing this from scratch).

Missing implementation part (that I would consider essential for real service): keeping list of
registered transactions so we can catch duplicates.

Systems that initiate transfers are responsible for generating transaction IDs 
(I always wanted to force this). Transaction IDs should be unique within given
client system ID. Globally unique transaction ID is 
`(requesting system ID, request ID)`.
This way we put burden on detecting duplicates on source systems - they have
most information to handle this. Also this helps to avoid using POST which 
has undefined semantics.

No authorization checks are performed.
Ideally "meta.by" fields must not be user-supplied, this information should be 
taken from session or authentication info. Not supported in current implementation.

In production code queries should be paged (not returning whole list at once) - not
implemented.

Normally I'd prefer to put common metadata such as `id`, creation timestamp, and creator 
id into a `.meta` data substructure, like
```text
{
  "meta": {
     "id": "123"
  },
  ... othe fiels here ...
}
```
I decided not to do this due to lack of time. So `id`s is just a field. 

Ideally we should provide more diagnostics (separately report overdraft or account duplicates etc.).

Need more request validation - check that `id`s in url match one in the data.  


## API

URL paths below are relative to API's root URL. Clients should specify complete URL.
See integration test's code in `petrlgad.txapi.AcceptanceTestKt.main` for usage example.

#### Create account

Requirements: account should not exist, initial value should not be negative.

```text
PUT /accounts/by-id/:id 
{
  id: <<account id>>,    
  currency: <<currency of account's value>>,
  value: <<account balance>> 
}
```

#### Make transfer

Requirements: source and accounts should not exist, currencies on both accounts should match,
transferred value should not exceed source account's balance.
After transaction `source_account.value -= amount` and `target_account.value += amount`.
Transfer amount must not be negative.

```text
PUT http://localhost:8080/transfers/by-owner/<<id of system that created this transfer>>/by-id/<<transfer id>>
{
  "id": <<transfer id>>,
  "by": <<id of system that created this transfer>>,
  "from_account_id": <<source account id>>,
  "to_account_id": <<target account id>>,
  "currency": <<currency of transfer>>,
  "amount": <<transfer amount>> 
}
```

#### List accounts

```text
GET /accounts/list
```

Returns list of accounts `[account-1, account-2, ...]` where account items have same data format 
as in create-account request.


### Example session

```text
GET http://localhost:8080/accounts/list 
```

``` 
>>> 200 OK
[]
```

```text
PUT http://localhost:8080/accounts/by-id/1 
{        
  "id": "1",  
  "currency": "EUR",
  "value": 400.0 
}
```

```
>>> 200 OK
```

```text
PUT http://localhost:8080/accounts/by-id/2 
{
  "id": "2",  
  "currency": "EUR",
  "value": 0.0 
}
```

```
>>> 200 OK
```

```text
GET http://localhost:8080/accounts/list 
```

``` 
>>> 200 OK
[
  {"id": "1", "currency": "EUR", "value": 400.0},
  {"id": "2", "currency": "EUR", "value": 0.0}
]
```


```text
PUT http://localhost:8080/transfers/by-owner/laundromat/by-id/1
{
  "id": "1",
  "by": "busy-dealers",
  "from_account_id": "1",
  "to_account_id": "2",
  "currency": "EUR",
  "amount": 45.0 
}
```

```
>>> 200 OK
```

```text
GET http://localhost:8080/accounts/list 
```

``` 
>>> 200 OK
[
  {"id": "1", "currency": "EUR", "value": 355.0},
  {"id": "2", "currency": "EUR", "value": 45.0}
]
```
