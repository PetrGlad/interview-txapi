# Transfer API


## Implementation notes

Platform - Java 11; Storage - in-memory H2 database; HTTP server - Jetty.

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

In production code queries should be paged (not returning whole list at once)- not
implemented.  


## API

URL paths are relative to API root URL. Clients should specify complete URL.

#### Create account

Preconditions: account should not exist, initial value should not be negative.
Account creation requests with duplicate `(meta.by, meta.id)` are ignored.

```text
PUT /accounts 
{
  "meta": {      
      id: <<account id>>
  },  
  currency: <<currency of account's value>>,
  value: <<account balance>> 
}
```

#### Make transfer

Preconditions: source and accounts should not exist, currencies on both accounts should match,
transferred value should not exceed source account balance.
After transaction `source_account.value -= value` and `target_account.value += value`.
Value must not be negative.  
Transactions with duplicate `(meta.by, meta.id)` are ignored.  

```text
PUT /transfers 
{
  "meta": {
      by: <<id of system that created this account>>,
      id: <<account id>>
  },  
  currency: <<currency of account's value>>,
  value: <<account balance>> 
}
```

#### List accounts

```text
GET /accounts
```

returns list of accounts `[account-1, account-2, ...]` where account items have same data 
as in create-account request.


### Example session

```text
GET http://localhost:8080/accounts 
```

``` 
>>> 200 OK
[]
```

```text
PUT http://localhost:8080/accounts 
{
  "meta": {      
      id: 1
  },  
  currency: "EUR",
  value: 400.0 
}
```

```
>>> 200 OK
```

```text
PUT http://localhost:8080/accounts 
{
  "meta": {      
      id: 2
  },  
  currency: "EUR",
  value: 0.0 
}
```

```
>>> 200 OK
```

```text
GET http://localhost:8080/accounts 
```

``` 
>>> 200 OK
[
{"meta": {id: 1}, currency: "EUR", value: 400.0},
{"meta": {id: 2}, currency: "EUR", value: 0.0}
]
```


```text
PUT http://localhost:8080/transfers 
{
  "meta": {
      by: "busy-dealers",
      id: "20200215-34"
  },  
  currency: "EUR",
  value: 45.0 
}
```

```
>>> 200 OK
```

``` 
>>> 200 OK
[
{"meta": {id: 1}, currency: "EUR", value: 355.0},
{"meta": {id: 2}, currency: "EUR", value: 45.0}
]
```
