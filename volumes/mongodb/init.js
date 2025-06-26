gipams = db.getSiblingDB('gipams_rmm_db');

gipams.createUser(
    {
        user: "RMM_user",
        pwd: "password",
        roles: [
            {
                role: "readWrite",
                db: "gipams_rmm_db"
            }
        ]
    }
);

gipams.createCollection('log_measurement');
gipams.createCollection('variable');
gipams.createCollection('detection_param');
gipams.createCollection('ip_blocking');

gipams.variable.insertOne(
    {
        "name": "IpBlockingExpirationTime",
        "value": 60
    }
)

gipams.detection_param.insertMany([
    {
        "variable": "CommonMaxAttemptsInAMinute",
        "name": "Maximum attempts in a minute",
        "description": "The maximum allowed number of login attempts in a minute from a single IP address.\nIf the rate exceeds this threshold, it may indicate a targeted attack.",
        "value": "11"
    },
    {
        "variable": "CommonMaxErrorsInAMinute",
        "name": "Maximum errors in a minute",
        "description": "The maximum allowed number of failed login attempts in a minute from a single IP address.\nIf the rate exceeds this threshold, it may indicate a targeted attack.",
        "value": "10"
    },
    {
        "variable": "CommonMaxTargetedAccounts",
        "name": "Maximum number of targeted accounts",
        "description": "The maximum number of unique user accounts that can be targeted by login attempts from a single IP address.\nExceeding this limit may indicate a coordinated attack targeting multiple accounts.",
        "value": "5"
    },
    {
        "variable": "CommonMaxErrorsSuccessions",
        "name": "Maximum error successions",
        "description": "The maximum number of consecutive failures groupings among successful logins from a single IP address.\nSample:\n\"consecutiveErrors\": {\n\"1707156336760\": 70,\n\"1707156336761\": 90,\n\"1707156336762\": 140,\n\"1707156336763\": 60,\n\"1707156336764\": 240,\n\"1707156336765\": 20\n} > Number of consecutive failures agroupations among successful logins = 6.\nHaving too many alternations between success and failure may indicate a brute-force attack.",
        "value": "1"
    },
    {
        "variable": "CommonMaxConsecutiveErrors",
        "name": "Maximum consecutive errors",
        "description": "The maximum number of consecutive failed login attempts allowed from a single IP address.\nSample:\n\"consecutiveErrors\": {\n\"1707156336760\": 70,\n\"1707156336761\": 90,\n\"1707156336762\": 140,\n\"1707156336763\": 60,\n\"1707156336764\": 240,\n\"1707156336765\": 20\n} > Maximum number of consecutive failed login attempts = 240.\nToo many consecutive failed attempts may indicate a brute-force attack.",
        "value": "10"
    },
    {
        "variable": "CommonMinAverageAttemptsInterval",
        "name": "Minimum average attempts interval",
        "description": "The minimum average time interval (in milliseconds) between consecutive login attempts from a single IP address.\nIf the average interval between attempts falls below this threshold, it may indicate automated or scripted login attempts, such as those in a brute-force attack.",
        "value": "1500"
    }
])