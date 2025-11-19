Application for detecting phishing in sms messages, from several banks' clients.

Legend:
sms message - message in json format send to appliction API, with scheme:
{
    sender: <SENDER_NUMBER>,
    receiver: <RECEIVER_NUMBER>,
    message: <BODY_OF_MESSAGE>
}
message - the actual content of sms message in txt format (<BODY_OF_MESSAGE>)

The client outline:
In this hypothetical scenario, client is said to be a group of banks, that reach out to me (a operator telekomunikacyjny) to act as a middleman in handling malicious sms messages, for their individual clients - one phone number is treated as a singular, individual client.

Main functionalities:
1. Phishing detector:
Every message that goes through system, assuming that client subscribies to service, is checked if it contains a URI. If it does, this URI is validated against Web Risk Google API and if it returns info that it is a phishing/malicious link, sms message is not store in database.
2. Subscription API:
Individual client can subscribe/unsubscribe to service, by sending a sms message in such a format to special number(000000000) with STOP message for unsubscribing or START message for subscribing:
{
    sender: <SENDER_NUMBER>,
    receiver: 000000000,
    message: START/STOP
}
Subscribers' numbers are stored in "subscribers" table.
When START received -> number is written to database.
When STOP received -> number is deleted from database.
3. SMS messages storing:
The service is responsible for storing of filtered messages. The client of given number have guarantee of having every single SMS message- that validates agaist Web Risk API - stored in database.

My assumptions:
- solution should be as cost efficcient as it can, because it probably gonna scale much
- solution should be prepared to scale easily - preferably scale out - and be rather lightweigt, to limit future costs when scaled
- app should be fairly readible and easy to understand and maintain for hypothetical future developers, should limit itself to main scenario but be prepared to do it properly, should be easy to spin up quickly and pushed to prod quickly
- service is responsible for storing every validated message in database
- this application is treated as mvp, that should be prepared for future expansion

Database schema:
- Subscribers table for storing telephone numbers of subscribers
- SMS table for storing validated sms messages in original format

Tech stack/tools that I used and why:
- Java language - it's the JVM language that I have most expierience with
- as a core Kafka Streams - the use case seems to be perfect for a stream tool -> it would benefit from being handled asynchroniously, hignly available and prepared for scaling out. I chose Kafka, since it integrates well with Java, was listed as one of technologies in job offer and I recently been working with Kafka on my data engineering program.
- Redis database - simmilary to Kafka, Redis seems ideal for such a use case of event-driven architecture -> I plan to keep only two tables, so there's no need for relational database. Redis is low latency, which SMS processing system values. High-performant for read/write operations(those the only ones I would really need). Good for caching, which would be the first issue to address after completing MVP of application, in my opinion. Is easier to implement than SQL DBs.
- TODO: choose testing tools
