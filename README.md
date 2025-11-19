Application for detecting phishing in sms messages, from several banks' clients.

Legend:
sms message - message in json format send to appliction API, with scheme:
{
    sender: <SENDER_NUMBER>,
    receiver: <RECEIVER_NUMBER>,
    message: <BODY_OF_MESSAGE>
}
message - the actual content of sms message in txt format (<BODY_OF_MESSAGE>)

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
- solution should be prepared to scale easily, so maybe kafka
- as one of notions in task, there was info that client wants it to be spined up quickly and to be cheap to 
maintain, so app should be readible and easy to understand for developers
- maybe I should separate full dev container with testing and container for production, but prolly not


main functionality:
- accepts requests with json of sms message
- checks if receiver number subscribes to phishing defender
- if not, stores message in DB 
- if yes, validates againts phishing interface, this google thingy
- if it does not validates -> does not store message
- else -> stores message

subscribing functionality:
- takes message like 
{
    sender: 99999999,
    receiver: SPECIAL_HARDCODED_NUMBER
    message: START/STOP
}
- stores number if started
- deltes number if stoped

will need two tables:
- one to store messages
- one to store phone numbers that subscribes to defender

