# Objective

The goal here is to create an API that returns flights information. Basically we're trying to develop an API that supplies aggregated flight searches results across multiple providers (skyscanner clone).

This gets materialized by a single endpoint `GET /api/flights`. 

# Context

The inventory is maintained by three providers:
 * Air-Jazz
 * Air-Moon


All these suppliers provide their inventory through a simple API. 

For the sake of this exercise, we assume that our providers only return one way flights. Therefore, for someone willing to search a return trip, we must combine two one way flights.

## Providers APIs


### Request

Both the suppliers have the same HTTP query format. This format is: 

```
/<provider name>/flights?depature_airport=...&arrival_airport=...&departure_date=...&return_date=...&tripType=...
```

Where:
 * Where provider name is either `jazz` or `moon`
 * depature_airport is the aiport code from which the user wants to leave
 * arrival_airport is the airport code to where the user wants to travel
 * departure_date is the desired departure date
 * return_date is the desired returning date
 * tripType is an enum indicating whether the trip is one-way (OW) or return (R)


All dates follow the format `YYYY-MM-DD`.
All datetimes follow format `YYYY-MM-DDTHH:mm:ss`.
Triptype enum is either `OW` or `R`.
Airport codes are three letters  [IATA Codes](https://en.wikipedia.org/wiki/IATA_airport_code) 
When the `tripType` is set to `OW` (One way), the `return_date` parameters is not required.
We assume all passengers are coming back to the same airport they left.



### Response

The responses differ between providers.

#### Air Moon

Air Moon response format follows schema:

```
[
  {
    price: : 105.56,
    flight: {
      id: "...",
      departure_airport: "CDG",
      arrival_airport: "LHR",
      departure_time: "2019-03-21T10:00:00", 
      arrival_time: "2019-03-21T11:05:00"
    }
  },
  ...

]

```

#### Air Moon

Air Moon response format follows schema:

```
[
  {
    price: : 105.56,
    legs: [
      {
        id: "...",
        departure_airport: "CDG",
        arrival_airport: "LHR",
        departure_time: "2019-03-21T10:00:00", 
        arrival_time: "2019-03-21T11:05:00"
      }
    ]
  },
  ...

]

```

Where the legs arrays contains all the flights information. This array always contains one element.


## Search features

### One ways combinable


As described before, return trips are made of two one way flights. 
So for instance, a user leaving from CDG (Paris) and going to LHR (London), will get back two one ways CDG <-> LHR and LHR <-> CDG.

To sum up, whenever a user hits our search endpoint (format specified later on), we do:

```
If tripType = R 
Then 
 |  For all our suppliers:
 |  Do
     | We search for a one way trip (with only the outgoing information)
     | We search for a one way trip (with only the returning information)
 |  End
 | 
 |  We combine all the one ways into returns flights
Else if tripType = OW
Then
| We only search one way trips with all our suppliers
End

We group the results
```


#### Combination rules

Two one way flights can only be combined if the departureTime of the returning fight is strictly after the arrivalTime of the outgoing flight.


### Grouping

Given the list of available flights, we want to group them according to their price, just like Opodo is doing:

![Grouping](images/opodo-grouping.png)

What we want is to group all the flights with the same price. If you look at the following scenario:


User searched for CDG <-> LHR on the same day. Within the results, 5 trips have the same price (100 €): 1 return trip and 4 combined one ways. Let's name these trips T1, T2, T3, T4, T5.
All these trips have outgoing and incoming flights such as :


| Flight  | Time  | Price |
|---|---|---|
| T1_Outgoing   | 2019-03-29T10:10  | 80 € | 
| T1_Returning  | 2019-03-29T18:00  | 20 € | 

| Flight  | Time  | Price |
|---|---|---|
| T2_Outgoing   | 2019-03-29T11:15  | 65 € |
| T2_Returning  | 2019-03-29T18:15  | 35 € |

| Flight  | Time  | Price |
|---|---|---|
| T3_Outgoing   | 2019-03-29T12:25  | 30 € |
| T3_Returning  | 2019-03-29T19:25  | 70 € |

| Flight  | Time  | Price |
|---|---|---|
| T4_Outgoing   | 2019-03-29T13:00  | 10 € |
| T4_Returning  | 2019-03-29T21:00  | 90 € |

| Flight  | Time  | Price |
|---|---|---|
| T5_Outgoing   | 2019-03-29T15:30  | 50 € |
| T5_Returning  | 2019-03-29T23:35  | 50 € |

If we were to group these flights according to the previous image, we would get someting like:

| Flight  | Time  | Price |
|---|---|---|
| T1_Outgoing  | 2019-03-29T10:10  | 80 € | 
| T2_Outgoing  | 2019-03-29T11:15  | 65 € |
| T3_Outgoing  | 2019-03-29T12:25  | 30 € |
| T4_Outgoing  | 2019-03-29T13:00  | 10 € |
| T5_Outgoing  | 2019-03-29T15:30  | 80 € |

___

| Flight  | Time  | Price |
|---|---|---|
| T1_Returning  | 2019-03-29T18:00  | 20 € | 
| T2_Returning  | 2019-03-29T18:15  | 35 € |
| T3_Returning  | 2019-03-29T19:25  | 70 € |
| T4_Returning  | 2019-03-29T21:00  | 90 € |
| T5_Returning  | 2019-03-29T09:35  | 20 € |



We direcly see that the grouping may cause problems as 
T1_Outgoing can be combined with T1_Returning, but it cannot be combined with T2_Returning as the price for this combination is no longer 100 € but rather 80 € + 35 € = 115 €.
Moreover, T1_Outgoing seems to be combinable with T5_Returning as their cumulated price is still 100 €, but if you look at the times, you'll see that the returning time is before the arrival time....

So on top of the flights and groups information, we need to store the available combinations.

### Response formats

The response is free of choice, but must contain at least:
 * The search parametes the user entered
 * The groups with their prices
 * The possible combinations within each group
 * The flights information (id, departure time, arrival time, airports, ...)






# Features

As described above, we aim to provide a single endpoint that will aggregate results from these three suppliers. Whenever one sends a `GET /api/flights` on our API, then the program should retrieve all results from the suppliers, sort them accordingly to their price (ascending), and limit the number of resuls to 50 flights.

Our API should return a json array containing the following schema:

```
[
  {
    "provider": "AIR_MOON|AIR_JAZZ|AIR_BEAM", // one of the supplier
    "price": <double>,
    "departure_time": <time>,
    "arrival_time": <time>
  }
]
```

Beyong these *basic* features, we will be interested in finding out how you would handle the following use cases / scenarios:
 * Provider `Air Moon` frequently takes a long time to respond (but it does send back data). Depending on the way you developed the API it may have performance impacts on the whole search. How would you take care of this ? 
 * Provider `Air Jazz` has downtime issues from time to time, and returns a `HTTP 502 Bad Gateway` error. Once again, how would you handle this so it does not penalize the whole API.
 * The API we just created is to be used by our partners. How would you handle security ? We need to make sure only authenticated users (and authorized) can access this API.
 * We would want to rate limit our API, so each of our client has a limited number of allowed calls. How would you handle this ?
 * Imagine we now have a lot of incoming traffic on our API, and there is some overlap on the search requests. How could we improve the program ?
 * Anything that you think could be relevant....

 

# Key points

The key points we will be looking at are:

 * Architecture and design
 * Code quality
 * Tests & testability
 * Tech choices

We know you may not have the time to make everything work fine, so it's ok to create dummy functions i.e functions that do nothing but are important for the process. 