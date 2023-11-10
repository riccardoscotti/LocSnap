const express = require('express');
const bodyParser = require('body-parser');
const { Client, Pool } = require('pg');
const cors = require('cors');
const { escapeIdentifier } = require('pg/lib/utils');
const { rows, user } = require('pg/lib/defaults');
const e = require('express');
const np = require('numjs')
const app = express();
const port = 8080;

async function sendQuery(query) {
    let statusCode = 401;
    let queryRes;
    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });
    await client.connect();

    try {
        queryRes = await client.query(query);
        queryRes = queryRes.rows
        statusCode = 200;
    } catch(error) {
        console.log(error)
        statusCode = 401;
    }

    await client.end();

    return {
        status: statusCode,
        queryRes: queryRes
    };
}

app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "http://localhost:3000");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    next();
});

app.use(bodyParser.json({limit: '50mb'}));

app.post('/check', (req, res) => {
    res.json({status: 200})
})

app.post('/checkcollectionexists', async (req, res) => {
    let statusCode;
    
    let query = `
        SELECT count(*) as count
        FROM collections
        WHERE
            collection_name = \'${req.body.collection_name}\' AND
            author = \'${req.body.logged_user}\'
    `
    
    await sendQuery(query)
    .then(response => {
        if (response.status === 200 && response.queryRes[0].count > 0) {
            statusCode = 200; // Collection exists
        } else {
            statusCode = 204; // Collection not exists
        }
    })

    res.json({
        status: statusCode
    })
})

app.post('/publish', async (req, res) => {
    const query = `
        UPDATE images
        SET public=true
        WHERE
            author=\'${req.body.logged_user}\' AND
            reference=\'${req.body.collection_name}\' AND
            image_name=\'${req.body.image_name}\'
    `;

    let queryRes = await sendQuery(query);
    let statusCode = queryRes.status;

    res.json({
        status: statusCode
    })
})

// User favorite type of photos
app.post('/recommend', async (req, res) => {
    let statusCode;
    var recommendedPlaces = [];
    
    let type_array = new Map();

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });
    
    let retrieveAllUsernamesQuery = `
        SELECT username as username
        FROM users
    `

    await client.connect();
    try {

        const resQuery1 = await client.query(retrieveAllUsernamesQuery);
        let user_array = [];
        
        resQuery1.rows.forEach(user => {
            user_array.push(user.username)
        })

        for (const user of user_array) {
            let user_types = [];
            const resQuery2 = await client.query(`
            SELECT type as type
            FROM images
            WHERE author=\'${user}\'`)

            resQuery2.rows.forEach(img => {
                user_types.push(img.type)
            })

            // Calculate user favorite type of photos
            // a is the array, c the item
            let counts = user_types.reduce((a, c) => {
                a[c] = (a[c] || 0) + 1;
                return a;
            }, {});

            let maxCount = Math.max(...Object.values(counts));
            mostFrequent = Object.keys(counts).filter(k => counts[k] === maxCount);

            // Map with {username: favorite_type}
            if (typeof mostFrequent[0] !== "undefined")
                type_array[user] = mostFrequent[0];
        }

        let similar_users = [];

        Object.keys(type_array).map(k => {
            if (k !== req.body.logged_user && // Not the logged_user constraint
            type_array[req.body.logged_user] === type_array[k]) { // Similarity between users
                
                similar_users.push(k) // is a similar user
            }
        })

        // Retrieve places visited by logged_user to avoid doubles
        let user_places = [];

        const resQuery3 = await client.query(`
            SELECT place as place
            FROM images
            WHERE author = \'${req.body.logged_user}\'
        `)

        resQuery3.rows.forEach(place => {
            user_places.push(place.place)
        })
        
        // Retrieve places visited by other similar users
        let other_users_places = [];

        for (const user of similar_users) {
            await client.query(`
                SELECT place as place
                FROM images
                WHERE author = \'${user}\'`)
                .then(response => {
                    response.rows.forEach(place => {
                        if (!other_users_places.includes(place)) {
                            other_users_places.push(place.place);
                        }
                    })
                })
        }

        // Returns occurrences of places in the previous array
        let counts = other_users_places.reduce((a, c) => {
            if (user_places.indexOf(c) == -1) // Place not visited by logged_user, so recommended.
                a[c] = (a[c] || 0) + 1;
            return a;
        }, {});

        const sortedArray = Object.entries(counts).sort((a, b) => b[1] - a[1]);

        // if (sortedArray.length >= 2) {
        //     // First 2 places, to be changed when more photos available.
        //     recommendedPlaces = [sortedArray[0][0], sortedArray[1][0]];
        //     statusCode = 200;
        // } else if (sortedArray.length == 1) {
        //     recommendedPlaces = [sortedArray[0][0]]
        //     statusCode = 200
        // } else {
        //     statusCode = 409
        // }

        if (sortedArray.length > 0) {
            for (index in sortedArray) {
                recommendedPlaces.push(sortedArray[index][0])
            }
            statusCode = 200;
        } else {
        
            statusCode = 409;
        }

    } catch (err) {
        console.log(err);
        recommendedPlaces = [];
        type_array = '' // Defaults
        statusCode = 401;
    }

    await client.end()
    res.json({
        status: statusCode,
        user_favorite_type: type_array[req.body.logged_user],
        recommendedPlaces: recommendedPlaces
    })
})

app.post('/search', async (req, res) => {
    let collections = {};
    const query = `
        SELECT collection_name as name
        FROM collections
        WHERE author=\'${req.body.logged_user}\' 
        AND collection_name ILIKE \'${req.body.search_text}%\'
    `;

    let result = await sendQuery(query);
    let statusCode = result.status;
    if(statusCode == 200) {
        result.queryRes.map((collection, index) => {
            collections[index] = collection
        })
    }
    res.json({
        status: statusCode,
        collections: collections
    })

});

app.post('/retrieveimages', async (req, res) => {
    let imgs = {};

    query = `
        SELECT image_name as name, ST_X(location) as lng, ST_Y(location) as lat
        FROM images
        WHERE author=\'${req.body.logged_user}\'
    `

    let result = await sendQuery(query);
    let statusCode = result.status;
    let queryRes = result.queryRes;

    let numColl = 0;
    if(statusCode == 200) {
        queryRes.forEach(img => {
            let tmp_img = {}
            tmp_img.name = img.name
            tmp_img.coords = [img.lat, img.lng]
            imgs[numColl] = tmp_img
            numColl++;
        })
    }

    res.json({
        status: statusCode,
        imgs: imgs
    })
})

app.post('/imagesof', async (req, res) => {
    var statusCode;
    var images = {};

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });
    await client.connect();

    try {
        const resQuery = await client.query(`
            SELECT image_name as name, image as image
            FROM images
            WHERE 
                author = \'${req.body.logged_user}\' AND
                reference = \'${req.body.collection_name}\'
        `)

        let index = 0;
        resQuery.rows.forEach(image => {
            let tmp_image = {}
            tmp_image.name = image.name
            tmp_image.image = image.image
            images[index] = tmp_image
            index++
        })

        statusCode = 200;

    } catch (error) {
        console.log(error);
        statusCode = 401;
    }

    await client.end()

    res.json({
        status: statusCode,
        images: images
    })
})

app.post('/updateimage', async (req, res) => {
    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });
    await client.connect();

    try {

        let queryRes = await client.query(`
            UPDATE images
            SET
                image_name = \'${req.body.new_image_name}\',
                public = \'${req.body.public}\',
                type = \'${req.body.type}\'
            WHERE 
                author = \'${req.body.logged_user}\' AND
                image_name = \'${req.body.old_image_name}\' AND
                reference = \'${req.body.collection_name}\'
        `)

        statusCode = 200;

    } catch (error) {
        console.log(error);
        statusCode = 401;
    }

    await client.end();

    res.json({
        status: statusCode
    })
})

async function clusterize(user, num_cluster) {
    let statusCode = 401;
    let clusters = {}

    let clusterDivisionQuery = `
        SELECT ST_ClusterKMeans(location, \'${num_cluster}\') OVER() as cid, 
                ST_X(location) as lng, ST_Y(location) as lat, image_name as image_name
        FROM images
        WHERE author=\'${user}\';`;
        
    let locateCentroidQuery = `
        SELECT
            ST_X(st_centroid(st_union(i.location))) as lng,
            ST_Y(st_centroid(st_union(i.location))) as lat,
            i.cid
        FROM (
            SELECT 
                ST_ClusterKMeans(location, \'${num_cluster}\') OVER() as cid, 
                location as location
            FROM images
            WHERE author=\'${user}\'
        ) as i
        GROUP BY i.cid`;

    let clusterDivisionRes = await sendQuery(clusterDivisionQuery);
    // let imageNameStatusCode = imageNameResult.status;
    
    let locateCentroidRes = await sendQuery(locateCentroidQuery);
    // let imagePositionStatusCode = imagePositionResult.status;
    
    if(clusterDivisionRes.status == 200 && locateCentroidRes.status == 200) {
        
        let cdResult = clusterDivisionRes.queryRes;
        let lcResult = locateCentroidRes.queryRes;
        
        for (let i = 0; i < num_cluster; i++) {
            clusters[i] = {};
            clusters[i].images = []
            clusters[i].centroid = []
            // clusters[i].location = []
            // clusters[i].image_names = []
            // clusters[i].coords = []
        }

        cdResult.forEach(image => {
            let tmp_image = {}
            tmp_image.image_name = image.image_name
            tmp_image.coords = [image.lat, image.lng]
            clusters[image.cid].images.push(tmp_image)
        })
        
        lcResult.forEach(cluster => {
            clusters[cluster.cid].centroid = [cluster.lat, cluster.lng]
        })
        
        statusCode = 200;
    }

    return {
        status: statusCode,
        clusters: clusters
    }
}

function secondDerivatives(inertiaValues) {
  const secondDerivatives = [];

  for (let i = 2; i < inertiaValues.length; i++) {
    const secondDerivative = inertiaValues[i] - 2 * inertiaValues[i - 1] + inertiaValues[i - 2];
    secondDerivatives.push(secondDerivative);
  }

  return secondDerivatives;
}

function findOptimalK(secondDerivatives) {
    for (let i = 0; i < secondDerivatives.length-1; i++) {
      if (secondDerivatives[i] < secondDerivatives[i+1]) {
        return i+2; // For index starts from 0, but clusters are 2+
      }
    }
    return secondDerivatives.length;
}

async function elbowClusterize(user) {
    let imageNumQuery = `SELECT COUNT(DISTINCT(location)) FROM images WHERE author=\'${user}\'`;
    let imageNumQueryRes = await sendQuery(imageNumQuery);
    if(imageNumQueryRes.status == 200) {
        let maxNum = parseInt(imageNumQueryRes.queryRes[0].count);
        if(maxNum == 1) {
            return clusterize(user, 1);
        }
        
        if(maxNum > 1){
            let inertias = [];
            for(let i = 2; i < maxNum + 1; i++) {
                let tmp_inertia = 0; // sum of squared distances between each point and centroid
                let clusteringResult = await clusterize(user, i);
                if(clusteringResult.status == 200) {
                    Object.entries(clusteringResult.clusters).map(cluster => {
                        Object.entries(cluster['1'].images).map(image => {

                            let image_distance = 
                                Math.pow((image[1].coords[0] - cluster['1'].centroid[0]), 2) +
                                Math.pow((image[1].coords[1] - cluster['1'].centroid[1]), 2);

                            tmp_inertia += image_distance
                        })
                    })
                }
                inertias.push(tmp_inertia)
            }

            return clusterize(user, findOptimalK(secondDerivatives(inertias)))
        }
    }
}

app.post('/clusterize', async (req, res) => {
    let result;
    if(req.body.elbow) {
        result = await elbowClusterize(req.body.logged_user)
    } else {
        result = await clusterize(req.body.logged_user, req.body.num_cluster)
    }

    res.json(result)
})

app.post('/maxclusternum', async (req, res) => {
    let imageNumQuery = `SELECT COUNT(DISTINCT(location)) FROM images WHERE author=\'${req.body.logged_user}\'`;
    let imageNumQueryRes = await sendQuery(imageNumQuery);

    let maxClusterNum;
    let statusCode = 401;

    if(imageNumQueryRes.status == 200) {
        maxClusterNum = parseInt(imageNumQueryRes.queryRes[0].count);
        statusCode = 200;
    }

    res.json({
        status: statusCode,
        maxClusterNum: maxClusterNum
    })
})

app.post('/imageupload', async (req, res) => {

    var statusCode
    const coll_name = req.body.collection_name
    const image64 = req.body.image
    const author = req.body.username
    const lat = req.body.lat
    const lon = req.body.lon
    const tagged_people = req.body.tagged_people
    const length = req.body.length
    const isPublic = req.body.public
    const type = req.body.type
    const place = req.body.place
    const image_name = req.body.image_name

    var tags = []
    var postgisPoint = "POINT("+lon+" "+lat+")"

    tagged_people?.forEach(tag => {
        tags.push(tag)
    })

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    try {

        query = `INSERT INTO collections (collection_name, author, length)
        VALUES (\'${coll_name}\', \'${author}\', \'${length}\');`

        await client.query(query);

        query2 = `INSERT INTO images (image_name, image, location, tagged_people, reference, public, type, place, author)
        VALUES (\'${image_name}\', \'${image64}\', 
        \'${postgisPoint}\', \'{${tags}}\', \'${coll_name}\', \'${isPublic}\', \'${type}\', \'${place}\', \'${author}\');`

        await client.query(query2);

        
        statusCode = 200;

    } catch (err) {
        statusCode = 401;
        console.log(err);
    }

    await client.end();
    
    res.json({status: statusCode})
});

app.post('/addtoexisting', async (req, res) => {

    var statusCode
    const coll_name = req.body.collection_name
    const image64 = req.body.image
    const author = req.body.username
    const lat = req.body.lat
    const lon = req.body.lon
    const tagged_people = req.body.tagged_people
    const length = req.body.length
    const isPublic = req.body.public
    const type = req.body.type
    const place = req.body.place
    const image_name = req.body.image_name

    var tags = []
    var postgisPoint = "POINT("+lon+" "+lat+")"

    tagged_people.forEach(tag => {
        tags.push(tag)
    })

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    try {

        await client.query(
            `INSERT INTO images (image_name, image, location, tagged_people, reference, public, type, place, author)
            VALUES (\'${image_name}\', \'${image64}\', 
            \'${postgisPoint}\', \'{${tags}}\', \'${coll_name}\', \'${isPublic}\', \'${type}\', \'${place}\', \'${author}\');`
        );

        // Update collection's length
        await client.query(`
            UPDATE collections
            SET length = length + 1
            WHERE 
                collection_name = \'${coll_name}\' AND 
                author = \'${author}\'
        `)

        statusCode = 200;

    } catch (err) {
        statusCode = 401;
        console.log(err);
    }
    await client.end();    
    res.json({status: statusCode})
});

app.post('/nearest', async (req, res) => {
    var statusCode;
    var imagesArray = [];
    var num_photos = req.body.num_photos
    var actualLat = req.body.actual_lat
    var actualLon = req.body.actual_lon

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect()

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

        statusCode = 200
        
    } catch (err) {
        statusCode = 401
        console.log(err);
    }

    await client.end();

    res.json({
        status: statusCode,
        images: imagesArray
    })

})

app.post('/deletephoto', async (req, res) => {
    let statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    try {
        // Delete single from as requested by user
        await client.query(`
            DELETE FROM images
            WHERE
                author = \'${req.body.logged_user}\' AND
                image_name = \'${req.body.image_name}\' AND
                reference = \'${req.body.collection_name}\'
            `)

        // Check collection's length
        await client.query(`
            SELECT length
            FROM collections
            WHERE
                collection_name = \'${req.body.collection_name}\' AND
                author = \'${req.body.logged_user}\'
        `)
        .then(async response => {
            if (response.rows[0].length == 1) { // Last photo in the collection, so deletion needed.
                await client.query(`
                    DELETE FROM collections
                    WHERE
                        collection_name = \'${req.body.collection_name}\' AND
                        author = \'${req.body.logged_user}\' 
                `)
            } else {
                // Decreasing by 1 the collection's length
                await client.query(`
                    UPDATE collections 
                    SET length = length-1
                    WHERE
                        collection_name = \'${req.body.collection_name}\' AND
                        author = \'${req.body.logged_user}\'
                `)
            }
        })
        .catch(error => {
            console.log(error);
        })

        statusCode = 200;

    } catch(error) {
        console.log(error);
        statusCode = 401
    }

    await client.end();
    
    res.json({
        status: statusCode
    })
})

app.post('/deletecollection', async (req, res) => {

    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

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
        
    } catch (err) {
        statusCode = 401;
        console.log(err);
    }

    await client.end()

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

    await client.connect();

    try {
        query_imgs = `
        UPDATE images as i
        SET tagged_people = CASE
		WHEN \'${req.body.friend}\'=ANY(tagged_people)
			THEN tagged_people
			ELSE array_append(tagged_people, \'${req.body.friend}\')
		END
		WHERE
            i.image_name=\'${req.body.image_name}\' AND
            i.author=\'${req.body.logged_user}\'
        `

        const resImgs = await client.query(query_imgs);

        if (resImgs.rowCount > 0) {
            statusCode = 200;
        } else {
            statusCode = 304; // Document not modified
        }
        
        
    } catch (err) {
        statusCode = 401;
        console.log(err);
    }

    await client.end()

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

    await client.connect();

    query = `SELECT user_2 as friend FROM friendships WHERE user_1=\'${req.body.logged_user}\';`

    try {
        const res = await client.query(query);

        res.rows.forEach(friendRow => {
            friends.push(friendRow.friend)
        });
        statusCode = 200;
    } catch (err) {
        statusCode = 401;
        console.log(err);
    }
    
    await client.end()
    res.json({
        status: statusCode,
        friends: friends});
    
})

app.post('/retrieve_public', async (req, res) => {
    var statusCode;
    var public_photos = {}

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    query = `
        SELECT image_name as name, ST_X(location) as lng, ST_Y(location) as lat, author as author
        FROM images
        WHERE
            author <> \'${req.body.logged_user}\' AND
            public = true
    `

    try {
        let index = 0;
        const resQuery = await client.query(query)
        resQuery.rows.forEach( row => {
            let tmp_img = {};
            tmp_img.name = row.name
            tmp_img.coords = [row.lat, row.lng]
            tmp_img.author = row.author
            public_photos[index] = tmp_img
            index++;
        })
        statusCode = 200
    } catch(err) {
        statusCode = 401
        console.log(err);
    }

    await client.end();
    res.json({
        status: statusCode,
        public_photos: public_photos
    });

})

app.post('/add_friend', async (req, res) => {

    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    query = `
            SELECT user_2
            FROM friendships
            WHERE
                user_1=\'${req.body.loggedUser}\' AND
                user_2=\'${req.body.newFriend}\';`

    try {
        const res = await client.query(query);
        if (res.rowCount > 0) {
            statusCode = 409; // Already friends
        } else {
            try {
                let query1 = `INSERT INTO friendships (user_1, user_2)
                            VALUES (\'${req.body.loggedUser}\', \'${req.body.newFriend}\');`
                
                let query2 = `INSERT INTO friendships (user_1, user_2)
                            VALUES (\'${req.body.newFriend}\', \'${req.body.loggedUser}\');`

                await client.query(query1)
                await client.query(query2)
                statusCode = 200
            } catch (err) {
                statusCode = 204;
            }
        }
    } catch (err) {
        statusCode = 401;
        console.log(err);
    }
    
    await client.end();
    res.json({status: statusCode});
    
})

app.post('/remove_friend', async (req, res) => {

    var statusCode;

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    const query1 = `
            DELETE
            FROM friendships
            WHERE
                user_1=\'${req.body.logged_user}\' AND
                user_2=\'${req.body.friend}\';`

    const query2 = `
            DELETE
            FROM friendships
            WHERE
                user_1=\'${req.body.friend}\' AND
                user_2=\'${req.body.logged_user}\';`

    try {
        const resQuery1 = await client.query(query1);
        const resQuery2 = await client.query(query2);

        statusCode = 200;
    } catch (err) {
        statusCode = 401;
        console.log(err);
    }
    
    await client.end();
    res.json({status: statusCode});
    
})

// Only used to display collection names on dashboard page
app.post('/retrievecollections', async (req, res) => {
    var statusCode;
    var retrieved_collections = [];

    const client = new Client({
        user: 'postgres',
        host: '127.0.0.1',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    try {
        // Distinct to avoid multi-photo collections to duplicate collection name
        // Also does not load collections with no photos in.
        const resQuery = await client.query(`
        SELECT DISTINCT c.collection_name as name, i.place as place
        FROM collections as c, images as i
        WHERE 
            c.collection_name = i.reference AND
            i.author=\'${req.body.logged_user}\'`);

        resQuery.rows.forEach(collection => {
            let tmp_coll = {}
            tmp_coll['name'] = collection.name
            tmp_coll['place'] = collection.place
            retrieved_collections.push(tmp_coll)
        })

        // Photos in which the user is tagged
        const resQuery2 = await client.query(`
        SELECT reference as name, place as place
        FROM images
        WHERE
            author <> \'${req.body.logged_user}\' AND
            \'${req.body.friend}\' = ANY(tagged_people)`);

        resQuery2.rows.forEach(collection => {
            let tmp_coll = {}
            tmp_coll['name'] = collection.name
            tmp_coll['place'] = collection.place
            retrieved_collections.push(tmp_coll)
        })

        // Public photos
        // const resQuery3 = await client.query(`
        // SELECT reference as name, place as place
        // FROM images
        // WHERE
        //     author <> \'${req.body.logged_user}\' AND
        //     public = true`);

        // resQuery3.rows.forEach(collection => {
        //     let tmp_coll = {}
        //     tmp_coll['name'] = collection.name
        //     tmp_coll['place'] = collection.place
        //     retrieved_collections.push(tmp_coll)
        // })

        statusCode = 200;
    } catch (err) {
        statusCode = 401; 
    }

    await client.end();

    res.json({
        status: statusCode,
        retrieved_collections: retrieved_collections
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

    await client.connect();

    query = `
        SELECT username
        FROM users
        WHERE
            username=\'${req.body.username}\' AND
            password=\'${req.body.password}\'`

    try {
        const resQuery = await client.query(query);
        if (resQuery.rowCount == 1) {
            statusCode = 200;
        } else {
            statusCode = 401;
        }
    } catch (err) {
        console.log(err);
    }
    await client.end();

    res.json({status: statusCode})
})

app.post('/signup', async (req, res) => {
    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'contextawarerc',
        port: 5432,
    });

    await client.connect();

    query = `INSERT INTO users (name, surname, username, password)
            VALUES (\'${req.body.name}\', \'${req.body.surname}\', \'${req.body.username}\', \'${req.body.password}\');`

    await client.query(query, (err, res) => {
        console.log(err, res);
    })

    await client.end()
    res.json({status: 200})

})

app.listen(port, () => {
    console.log('Server on');
})
