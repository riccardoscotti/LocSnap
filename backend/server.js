const express = require('express');
const bodyParser = require('body-parser');
const { Client, Pool } = require('pg');
const cors = require('cors');
const { escapeIdentifier } = require('pg/lib/utils');
const app = express();
const port = 8080;

const client_info = {
    user: 'postgres',
    host: '0.0.0.0',
    database: 'contextawarerc',
    port: 5432,
}

app.use(cors({
    origin: 'http://localhost:3000'
}));

app.use(bodyParser.json({limit: '50mb'}));

app.post('/check', (req, res) => {
    res.json({status: 200})
})

app.post('/search', async (req, res) => {
    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    let collections = {};
    let statusCode;
    const query = `
        SELECT collection_name
        FROM collections
        WHERE author='${req.body.logged_user}' 
        AND collection_name LIKE '${req.body.search_text}%'
    `;
    client.connect();

    try {
        const resQuery = await client.query(query);
        if(resQuery.rowCount > 0) {
            
            resQuery.rows.map( (collection, index) => {
                collections[index] = collection
            })
            statusCode = 200;
        } else {
            statusCode = 204;
        }         
    } catch {
        statusCode = 401;
    }
    
    client.end()
    res.json({
        status: statusCode,
        collections: collections
    })

});

app.post('/clusterize', async (req, res) => {

    var statusCode;
    var clusters = {}
    for (let i = 0; i < req.body.num_cluster; i++) {
        clusters[i] = {};
        clusters[i].image_names = []
        clusters[i].coords = []
    }

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });
    client.connect();

    let query = `
        SELECT ST_ClusterKMeans(location, ${req.body.num_cluster}) OVER() as cid, image_name as image_name
        FROM images
        WHERE author=\'${req.body.logged_user}\'
    `
    try {

        // Assign each photo to cluster
        let resQuery = await client.query(query);
        if (resQuery.rowCount > 0) {
            resQuery.rows.forEach(cluster => {
                clusters[cluster.cid].image_names.push(cluster.image_name)
            })
        }

        let query2 = `
            SELECT	ST_ClusterKMeans(i.location, ${req.body.num_cluster}) OVER() as cid, 
                    ST_X(ST_Centroid(ST_Collect(ST_SetSRID(i.location, 4326)))) AS lng,  
                    ST_Y(ST_Centroid(ST_Collect(ST_SetSRID(i.location, 4326)))) AS lat
            FROM images as i, collections as c
            WHERE i.reference=c.collection_name and c.author=\'${req.body.logged_user}\'
            GROUP BY i.location
            `

        let resQuery2 = await client.query(query2);
        if (resQuery2.rowCount > 0) {
            resQuery2.rows.forEach(cluster => {
                clusters[cluster.cid].coords.push(cluster.lat)
                clusters[cluster.cid].coords.push(cluster.lng)
            })
        }
                    
        statusCode = 200;
        client.end();

    } catch {
        statusCode = 204;
        client.end();
    }
    res.json({
        status: statusCode,
        clusters: clusters
    })
})

app.post('/imageupload', async (req, res) => {

    var statusCode
    const name = req.body.name
    const image64 = req.body.image
    const author = req.body.username
    const lat = req.body.lat
    const lon = req.body.lon
    const tagged_people = req.body.tagged_people
    const length = req.body.length

    var imagesArray = [];
    var tags = []
    var postgisPoint = "POINT("+lon+" "+lat+")"

    image64.forEach(image => {
        imagesArray.push(image);
    });

    tagged_people.forEach(tag => {
        tags.push(tag)
    })

    try {

        query = `INSERT INTO collections (collection_name, author, length)
        VALUES (\'${name}\', \'${author}\', \'${length}\');`

        const client = new Client({
            user: 'postgres',
            host: '0.0.0.0',
            database: 'contextawarerc',
            port: 5432,
        });
    
        client.connect();
        await client.query(query);
        client.end();

        const pool = new Pool({
            host: '0.0.0.0',
            user: 'postgres',
            database: 'contextawarerc',
            max: 100,
            idleTimeoutMillis: 30000,
            connectionTimeoutMillis: 5000,
        })
    
        var index = 1;
        imagesArray.forEach(image => {

            query = `INSERT INTO images (image_name, image, location, tagged_people, reference)
            VALUES (\'${index}_${name}\', \'${image}\', 
            \'${postgisPoint}\', \'{${tags}}\', \'${name}\');`

            pool.query(query);
            index++;
        })

        pool.end();
        statusCode = 200;

    } catch (err) {
        statusCode = 401;
        console.log(err);
    }
    
    res.json({status: statusCode})
});

app.post('/nearest', async (req, res) => {
    var imagesArray = [];
    var num_photos = req.body.num_photos
    var actualLat = req.body.actual_lat
    var actualLon = req.body.actual_lon

    console.log(actualLat, actualLon)

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect()

    query = `
        SELECT i.image as imageresult
        FROM images as i
        ORDER BY ST_Distance(\'POINT(${actualLon} ${actualLat})\'::geometry, i.location) 
        LIMIT ${num_photos}
        `

    try {
        const result = await client.query(query);
        result.rows.forEach(element => {
            imagesArray.push(element.imageresult)
        });
        client.end();
        res.json({
            status: 200,
            images: imagesArray
        })
    } catch (err) {
        console.log(err);
        res.json({
            status: 401
        })
    }
})

app.post('/deletecollection', async (req, res) => {

    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect();

    try {

        query_imgs = `
        DELETE
        FROM images as i
        USING collections as c
        WHERE
            i.reference=c.collection_name AND
            i.reference=\'${req.body.collection_name}\' AND
            c.author=\'${req.body.logged_user}\';
        `

        query_coll = `
        DELETE
        FROM collections as c
        WHERE 
            c.collection_name=\'${req.body.collection_name}\' AND
            c.author=\'${req.body.logged_user}\';
        `

        const resImgs = await client.query(query_imgs);
        const resColl = await client.query(query_coll);
        
        if (resColl.rowCount > 0 && resImgs.rowCount > 0) {
            statusCode = 200;
        } else {
            statusCode = 304 // Document not modified
        }
        client.end();
        
    } catch (err) {
        statusCode = 401;
        console.log(err);
    }

    res.json({
        status: statusCode
    })
})

app.post('/tag_friend', async (req, res) => {

    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect();

    try {
        query_imgs = `
        UPDATE images as i
        SET tagged_people = CASE
		WHEN \'${req.body.friend}\'=ANY(tagged_people)
			THEN tagged_people
			ELSE array_append(tagged_people, \'${req.body.friend}\')
		END
        FROM collections as c
		WHERE
            i.reference=\'${req.body.collection_name}\' AND
            i.reference=c.collection_name AND
            c.author=\'${req.body.logged_user}\'
        `

        const resImgs = await client.query(query_imgs);

        if (resImgs.rowCount > 0) {
            statusCode = 200;
        } else {
            statusCode = 304; // Document not modified
        }
        
        client.end();
        
    } catch (err) {
        statusCode = 401;
        console.log(err);
    }

    res.json({
        status: statusCode
    })
})

app.post('/get_friends', async (req, res) => {

    var statusCode;
    var friends = [];

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect();

    query = `SELECT user_2 as friend FROM friendships WHERE user_1=\'${req.body.loggedUser}\';`

    try {
        const res = await client.query(query);
        if (res.rowCount > 0) {
            res.rows.forEach(friendRow => {
                friends.push(friendRow.friend)
            });
            statusCode = 200;
        } else {
            console.log("No friends available.");
            statusCode = 401;
        }
        client.end();
    } catch (err) {
        console.log(err);
    }

    res.json({
        status: statusCode,
        friends: friends});
    
})

app.post('/add_friend', async (req, res) => {

    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect();

    query = `
            SELECT user_2
            FROM friendships
            WHERE
                user_1=\'${req.body.loggedUser}\' AND
                user_2=\'${req.body.newFriend}\';`

    try {
        const res = await client.query(query);
        if (res.rowCount > 0) {
            statusCode = 409;
        } else {
            try {
                let query1 = `INSERT INTO friendships (user_1, user_2)
                            VALUES (\'${req.body.loggedUser}\', \'${req.body.newFriend}\');`
                
                let query2 = `INSERT INTO friendships (user_1, user_2)
                            VALUES (\'${req.body.newFriend}\', \'${req.body.loggedUser}\');`

                await client.query(query1)
                await client.query(query2)
                statusCode = 200
                console.log("Amicizia aggiunta")
            } catch (err) {
                statusCode = 204
                console.log("Utente non trovato")
            }
        }
        client.end();
    } catch (err) {
        console.log(err);
    }

    res.json({status: statusCode});
    
})

// Only used to display collection names on dashboard page
app.post('/retrievecollections', async (req, res) => {
    var statusCode;
    var retrieved_collections = {};

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect();
    query = `
            SELECT collection_name as name
            FROM collections
            WHERE author=\'${req.body.logged_user}\';`

    try {
        const res = await client.query(query);
        if (res.rowCount > 0) {
            var numColl = 0;
            res.rows.forEach(collection => {
                let tmp_collection = {};
                tmp_collection.name = collection.name
                retrieved_collections[numColl] = tmp_collection
                numColl++;
            })
            statusCode = 200;
        } else {
            statusCode = 401;
        }
        client.end();
    } catch (err) {
        console.log(err);
    }

    res.json({
        status: statusCode,
        retrievedCollections: retrieved_collections
    })
    
})

app.post('/login', async (req, res) => {

    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect();

    query = `SELECT username FROM users WHERE username=\'${req.body.username}\' 
            AND password=\'${req.body.password}\';`

    try {
        const res = await client.query(query);
        if (res.rowCount == 1) {
            console.log("Login successful.");
            statusCode = 200;
        } else {
            console.log("Login incorrect.");
            statusCode = 401;
        }
        client.end();
    } catch (err) {
        console.log(err);
    }

    res.json({status: statusCode})
    
})

app.post('/signup', (req, res) => {
    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    client.connect();

    query = `INSERT INTO users (name, surname, username, password)
            VALUES (\'${req.body.name}\', \'${req.body.surname}\', \'${req.body.username}\', \'${req.body.password}\');`

    client.query(query, (err, res) => {
        console.log(err, res);
        client.end();
    })

    res.json({status: 200})

})

app.listen(port, () => {
    console.log('Server on');
})