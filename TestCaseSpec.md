# Test Case Spec
## Execution Strategy
### Reserve Flow
There are 7 steps in booking flow test case:
+ Ticket Reserve Step 1: - Login
+ Ticket Reserve Step 2: - Ticket Booking
+ Ticket Reserve Step 3: - Select Contacts
+ Ticket Reserve Step 4: - Confirm Your Ticket Information
+ Ticket Reserve Step 5: - Pay for Preserve Ticket
+ Ticket Reserve Step 6: - Ticket Collect
+ Ticket Reserve Step 7: - Enter Station

To generate ticket with different status, there are 4 execution sequence in reserve flow:
1. Step 1 - 4, ticket status: `not paid`

2. Step 1 - 5, ticket status: `paid & not collected`

3. Step 1 - 6, ticket status: `collected`

4. Step 1 - 7, ticket status: `used`

  Each test case will randomly execute one of the processes that mentioned obove.
### Cancel Flow
Tere are 4 steps in canceling flow test case:
+ Ticket Cancel Step 1: - Login
+ Ticket Cancel Step 2: - Query Order
+ Ticket Cancel Step 3: - Calculate Refund
+ Ticket Cancel Step 4: - Cancel Order

There are 3 execution squence in canceling flow:
1. Step 1 - 2, ticket status is not in `[not paid, paid & not collected]`
2. Step 1 - 3, ticket status is in `[not paid, paid & not collected]`, the user can continue to execute fourth steps or give up. 
3. Step 1 - 4, ticket status is in `[not paid, paid & not collected, rebook]`
  The specific test case sequence is depends on the ticket status and user behaviour.

## Dynamic Parameter Strategy  --TODO
Each test case will randomly select one user.
The parameters of each test method are random.

## Assert Strategy --TODO
Using custom exception.
Assert the response status code and response DTO.

## BUG injection --TODO
injecting 3 dimension bugs in each flow.
