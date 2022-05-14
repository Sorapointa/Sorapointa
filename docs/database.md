# Database Operation Safety

## Transaction

Nested use of `transaction` is prohibited when it is not required, 
which would result in sessions started at `READ_COMMITTED` and above isolation levels 
not being able to read any changes within the nested transaction, 
resulting in various unintended and unpredictable outcomes, 
so transactions cannot be created within API methods, 
and transactions must be guaranteed to be invoked 
by the outermost code, not by the api layer.

Any error during the execution of the transaction 
will cause the entire operation of the transaction to be rolled back.

### Transaction Isolation

The reason for the above requirement is that the Repeatable Read isolation level 
only sees data committed before the transaction began; 
it never sees either uncommitted data or changes committed during transaction execution by concurrent transactions. 
(However, the query does see the effects of previous updates executed within its own transaction, 
even though they are not yet committed.)

This level is different from Read Committed in that a query in a repeatable read transaction 
sees a snapshot as of the start of the first non-transaction-control statement in the transaction, 
not as of the start of the current statement within the transaction. 
Thus, successive `SELECT` commands within a single transaction see the same data, 
i.e., they do not see changes made by other transactions 
that committed after their own transaction started.

So if you use nested transactions, 
it will make outer transaction could not see updates from the inner transaction, 
and further cause unintended results.

Refers to [PostgreSQL Manual - Transaction Isolation](https://www.postgresql.org/docs/current/transaction-iso.html)

### Asynchronous Transactions

Asynchronous transactions are allowed, 
but it is not allowed to switch threads / coroutine in the same transaction 
to operate on the database. 
This will cause deadlocks during high concurrency. 
All database operations within the same transaction must be 
executed synchronously under the same thread.

## Table Structure

Once the table structure is defined, 
it is best not to make any changes (including deleting fields and modifying field properties).
Because it will require user to manually sync new table structure, 
only adding fields can be updated automatically.

## API Method

Any database operations API provided to the upper layer should not include any transaction block.
It will cause nested transactions and raises the issues mentioned above due to transaction isolation.

Database operation methods must add the `suspend` modifier.
and suppress warnings with `@Suppress("RedundantSuspendModifier")`.
