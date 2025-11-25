// Switch to restaurant_events database
db = db.getSiblingDB('restaurant_events');

// Create application user
db.createUser({
    user: 'restaurant_user',
    pwd: 'restaurant_pass',
    roles: [
        {
            role: 'readWrite',
            db: 'restaurant_events'
        }
    ]
});

// Create collections with validators
db.createCollection('events', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['aggregateId', 'aggregateType', 'eventType', 'timestamp', 'version'],
            properties: {
                aggregateId: {
                    bsonType: 'string',
                    description: 'Aggregate ID is required'
                },
                aggregateType: {
                    bsonType: 'string',
                    description: 'Aggregate type is required'
                },
                eventType: {
                    bsonType: 'string',
                    description: 'Event type is required'
                },
                eventData: {
                    bsonType: 'string',
                    description: 'Event data as JSON string'
                },
                userId: {
                    bsonType: 'string'
                },
                timestamp: {
                    bsonType: 'date',
                    description: 'Timestamp is required'
                },
                version: {
                    bsonType: 'long',
                    description: 'Version number is required'
                },
                metadata: {
                    bsonType: 'string'
                }
            }
        }
    }
});

// Create indexes for performance
db.events.createIndex({ 'aggregateId': 1, 'version': 1 }, { unique: true });
db.events.createIndex({ 'aggregateId': 1, 'timestamp': -1 });
db.events.createIndex({ 'eventType': 1, 'timestamp': -1 });
db.events.createIndex({ 'timestamp': -1 });
db.events.createIndex({ 'userId': 1, 'timestamp': -1 });

// Create collection for read models (CQRS)
db.createCollection('booking_read_model');
db.booking_read_model.createIndex({ 'bookingId': 1 }, { unique: true });
db.booking_read_model.createIndex({ 'customerEmail': 1 });
db.booking_read_model.createIndex({ 'status': 1 });
db.booking_read_model.createIndex({ 'bookingTime': 1 });
db.booking_read_model.createIndex({ 'timeSlot': 1 });

// Create collection for snapshots (performance optimization)
db.createCollection('snapshots');
db.snapshots.createIndex({ 'aggregateId': 1, 'version': -1 });

print('MongoDB initialization completed successfully!');