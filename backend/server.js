const express = require('express');
const bodyParser = require('body-parser');
const { Client, Pool } = require('pg');
const cors = require('cors');
const { escapeIdentifier } = require('pg/lib/utils');
const { rows, user } = require('pg/lib/defaults');
const e = require('express');
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
    } catch (error) {
        console.log(error)
        statusCode = 401;
    }

    await client.end();

    return {
        status: statusCode,
        queryRes: queryRes
    };
}

app.use(function (req, res, next) {
    res.header("Access-Control-Allow-Origin", "http://localhost:3000");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    next();
});

app.use(bodyParser.json({ limit: '50mb' }));

app.post('/check', (req, res) => {
    res.json({ status: 200 })
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
    let loggedUser = req.body.logged_user;

    let statusCode = 401;
    let favoriteType;
    let recommendedPlaces = [];

    // retrieving the favorite types for every user
    let favoriteTypesQuery = `
            select author, mode() within group (order by type) as type
            from images
            group by author`;
    
    let favoriteTypesResult = await sendQuery(favoriteTypesQuery);
    let favoriteTypesStatus = favoriteTypesResult.status;
    let favoriteTypesQueryRes = favoriteTypesResult.queryRes;

    
    if(favoriteTypesStatus == 200) {
        let favoriteTypes = {};
        favoriteTypesQueryRes.map(r => {
            favoriteTypes[r.author] = r.type;
        });
        
        // logged user's favorite type
        favoriteType = favoriteTypes[loggedUser];

        // retrieving similar users
        // in other words, users whose favorite type is the same as logged user's
        let similarUsers = Object.keys(favoriteTypes).filter(k => {
            return (k !== loggedUser) && (favoriteTypes[k] === favoriteType);
        });

        // retrieving random places from similar users
        let recommendedPlacesQuery = `
            select place
            from images
            where ${similarUsers.map(user => `author = '${user}'`).join(' or ')}
                and type = '${favoriteType}';
        `;
        
        let recommendedPlacesResult = await sendQuery(recommendedPlacesQuery);
        let recommendedPlacesStatus = recommendedPlacesResult.status;
        let recommendedPlacesQueryRes = recommendedPlacesResult.queryRes;

        if(recommendedPlacesStatus == 200) {
            let allRecommendedPlaces = recommendedPlacesQueryRes.map(r => r.place);

            // we take at most 8 random places from all the possible recommended places
            recommendedPlaces = allRecommendedPlaces
                                    .sort(() => Math.random() - 0.5)
                                    .slice(0, 8);
                    
            statusCode = 200;
        }
    }

  
    res.json({
        status: statusCode,
        user_favorite_type: favoriteType,
        recommendedPlaces: recommendedPlaces
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
    if (statusCode == 200) {
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
    let statusCode;
    let images = {};

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

app.post('/search', async (req, res) => {

    let collections = [];
    let statusCode;

    let searchQuery = await sendQuery(`
        SELECT c.collection_name as name, i.place place
        FROM collections as c, images as i
        WHERE
            c.author='${req.body.logged_user}' AND
            i.reference = c.collection_name AND 
            c.collection_name LIKE '${req.body.search_text}%'
        GROUP BY i.place, c.collection_name`
    )

    if(searchQuery.queryRes.length > 0) {
        searchQuery.queryRes.map( (collection, index) => {
            let tmp_coll = {}
            tmp_coll["name"] = collection.name
            tmp_coll["place"] = collection.place
            collections.push(tmp_coll)
        })
        statusCode = 200;
    } else {
        statusCode = 204;
    }

    res.json({
        status: statusCode,
        collections: collections
    })

});

app.post('/updateimage', async (req, res) => {
    let statusCode;

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
        SELECT ST_ClusterKMeans(location, ${num_cluster}) OVER() as cid, 
                ST_X(location) as lng, ST_Y(location) as lat, image_name as image_name
        FROM images
        WHERE author=\'${user}\'
    `;

    let locateCentroidQuery = `
        SELECT
            ST_X(st_centroid(st_union(i.location))) as lng,
            ST_Y(st_centroid(st_union(i.location))) as lat,
            i.cid
        FROM (
            SELECT 
                ST_ClusterKMeans(location, ${num_cluster}) OVER() as cid, 
                location as location
            FROM images
            WHERE author=\'${user}\'
        ) as i
        GROUP BY i.cid`;

    let clusterDivisionRes = await sendQuery(clusterDivisionQuery);

    let locateCentroidRes = await sendQuery(locateCentroidQuery);

    if (clusterDivisionRes.status == 200 && locateCentroidRes.status == 200) {

        let cdResult = clusterDivisionRes.queryRes;
        let lcResult = locateCentroidRes.queryRes;

        for (let i = 0; i < num_cluster; i++) {
            clusters[i] = {};
            clusters[i].images = []
            clusters[i].centroid = []
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

function secondDerivativesFunc(inertiaValues) {
    const secondDerivatives = [];

    for (let i = 2; i < inertiaValues.length; i++) {
        const secondDerivative = inertiaValues[i] - 2 * inertiaValues[i - 1] + inertiaValues[i - 2];
        secondDerivatives.push(secondDerivative);
    }

    return secondDerivatives;
}

function findOptimalK(secondDerivatives) {

    // If length equals 2 it means we have 3 maximum 3 clusters.
    // Having 3 clusters means 3 different locations.
    // It's normal that iterating through the list will return 2,
    // since returning 3 means having inertia = 0 and one photo per cluster. 
    if (secondDerivatives.length <= 2) {
        return 2;
    }

    for (let i = 0; i < secondDerivatives.length - 1; i++) {
        if (secondDerivatives[i] < secondDerivatives[i + 1]) {
            return i+2; // For index starts from 0, but clusters are 2+
        }
    }

    // Default, no improvement with cluster augmentation.
    return 2;
}

async function elbowClusterize(user) {
    let imageNumQuery = `SELECT COUNT(DISTINCT(location)) FROM images WHERE author=\'${user}\'`;
    let imageNumQueryRes = await sendQuery(imageNumQuery);

    if (imageNumQueryRes.status == 200) {
        let maxNum = parseInt(imageNumQueryRes.queryRes[0].count);
        if (maxNum == 1) {
            return clusterize(user, 1);
        } else if (maxNum == 2) {
            return clusterize(user, 2);
        }

        if (maxNum > 2) {
            let inertias = [];
            for (let i = 2; i < (maxNum + 1); i++) {
                let tmp_inertia = 0; // sum of squared distances between each point and centroid

                let clusteringResult = await clusterize(user, i);
                if (clusteringResult.status == 200) {
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


            return clusterize(user, findOptimalK(secondDerivativesFunc(inertias)))
        }
    }
}

app.post('/clusterize', async (req, res) => {
    let result;
    if (req.body.elbow) {
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

    if (imageNumQueryRes.status == 200) {
        maxClusterNum = parseInt(imageNumQueryRes.queryRes[0].count);
        statusCode = 200;
    }

    res.json({
        status: statusCode,
        maxClusterNum: maxClusterNum
    })
})

// Uploads photo, creating a new single-photo collection
app.post('/imageupload', async (req, res) => {

    let statusCode;
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

    let tags = []
    let postgisPoint = "POINT(" + lon + " " + lat + ")"

    tagged_people?.forEach(tag => {
        tags.push(tag)
    })

    let createCollRes = await sendQuery(`
        INSERT INTO collections (collection_name, author, length)
        VALUES (\'${coll_name}\', \'${author}\', \'${length}\');
    `)

    let createImageRes = await sendQuery(`
        INSERT INTO images (image_name, image, location, tagged_people, reference, public, type, place, author)
        VALUES (
            \'${image_name}\',
            \'${image64}\',
            \'${postgisPoint}\',
            \'{${tags}}\', 
            \'${coll_name}\',
            \'${isPublic}\', 
            \'${type}\', 
            \'${place}\', 
            \'${author}\'
        );`
    )

    if (createCollRes.status == 200 && createImageRes.status == 200) {
        statusCode = 200;
    } else {
        statusCode = 401;
    }

    res.json({ status: statusCode })
});

app.post('/addtoexisting', async (req, res) => {

    let statusCode
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

    let tags = []
    let postgisPoint = "POINT(" + lon + " " + lat + ")"

    tagged_people.forEach(tag => {
        tags.push(tag)
    })

    let createImageQuery = await sendQuery(
        `INSERT INTO images (image_name, image, location, tagged_people, reference, public, type, place, author)
        VALUES (
            \'${image_name}\',
            \'${image64}\',
            \'${postgisPoint}\',
            \'{${tags}}\',
            \'${coll_name}\',
            \'${isPublic}\',
            \'${type}\',
            \'${place}\',
            \'${author}\'
        );`
    );

    // Update collection's length
    let updateCollectionQuery = await sendQuery(`
        UPDATE collections
        SET length = length + 1
        WHERE 
            collection_name = \'${coll_name}\' AND 
            author = \'${author}\'
    `)

    if (createImageQuery.status == 200 && updateCollectionQuery.status == 200) {
        statusCode = 200;
    } else {
        statusCode = 401;
    }

    res.json({ status: statusCode })
});

app.post('/nearest', async (req, res) => {
    let statusCode;
    let imagesArray = [];
    let num_photos = req.body.num_photos
    let actualLat = req.body.actual_lat
    let actualLon = req.body.actual_lon
    let loggedUser = req.body.logged_user

    // Returns user's photos and other users' public ones
    const nearestPhotosQuery = await sendQuery(`
        SELECT image as image, image_name as image_name
        FROM images
        WHERE
            (author = \'${loggedUser}\') OR 
            (author <> \'${loggedUser}\' AND public=true)
        ORDER BY ST_Distance(\'POINT(${actualLon} ${actualLat})\'::geometry, location) 
        LIMIT ${num_photos}
    `);

    nearestPhotosQuery.queryRes.forEach(element => {
        let tmp_img = {}
        tmp_img.image = element.image
        tmp_img.image_name = element.image_name

        imagesArray.push(tmp_img)
    });

    res.json({
        status: nearestPhotosQuery.status,
        images: imagesArray
    })

})

app.post('/deletephoto', async (req, res) => {
    let statusCode;

        // Delete single from as requested by user
        let deletePhotoQuery = await sendQuery(`
            DELETE FROM images
            WHERE
                author = \'${req.body.logged_user}\' AND
                image_name = \'${req.body.image_name}\' AND
                reference = \'${req.body.collection_name}\'
            `)

        // Check collection's length
        let checkLengthQuery = await sendQuery(`
            SELECT length
            FROM collections
            WHERE
                collection_name = \'${req.body.collection_name}\' AND
                author = \'${req.body.logged_user}\'
        `)

        // Last photo in the collection, so deletion needed.
        if (checkLengthQuery.queryRes[0].length == 1) { 
            await sendQuery(`
                DELETE FROM collections
                WHERE
                    collection_name = \'${req.body.collection_name}\' AND
                    author = \'${req.body.logged_user}\' `
            )
        } else {
            // Decreasing by 1 the collection's length
            await sendQuery(`
                UPDATE collections 
                SET length = length-1
                WHERE
                    collection_name = \'${req.body.collection_name}\' AND
                    author = \'${req.body.logged_user}\'`
            )
        }

        if (deletePhotoQuery.status == 200 && checkLengthQuery.status == 200) {
            statusCode = 200
        } else {
            statusCode = 401
        }

    res.json({
        status: statusCode
    })
})

app.post('/deletecollection', async (req, res) => {

    let statusCode;

    let deleteImagesQuery  = await sendQuery(`
        DELETE
        FROM images as i
        USING collections as c
        WHERE
            i.reference=c.collection_name AND
            i.reference=\'${req.body.collection_name}\' AND
            c.author=\'${req.body.logged_user}\';`
    )

    let deleteCollectionQuery = await sendQuery(`
        DELETE
        FROM collections as c
        WHERE 
            c.collection_name=\'${req.body.collection_name}\' AND
            c.author=\'${req.body.logged_user}\';
        `
    )

    if (deleteImagesQuery.queryRes.length > 0 && deleteCollectionQuery.queryRes.length > 0) {
        statusCode = 200;
    } else {
        statusCode = 304 // Document not modified
    }

    res.json({
        status: statusCode
    })
})

app.post('/tag_friend', async (req, res) => {

    let statusCode;

    let tagFriendQuery = await sendQuery(`
        UPDATE images as i
        SET tagged_people = CASE
        WHEN \'${req.body.friend}\'=ANY(tagged_people)
            THEN tagged_people
            ELSE array_append(tagged_people, \'${req.body.friend}\')
        END
        WHERE
            i.image_name=\'${req.body.image_name}\' AND
            i.author=\'${req.body.logged_user}\'`
    )

    res.json({
        status: tagFriendQuery.status
    })
})

app.post('/get_friends', async (req, res) => {

    let friends = [];

    let getFriendsQuery = await sendQuery(
        `SELECT user_2 as friend
        FROM friendships
        WHERE user_1=\'${req.body.logged_user}\';`
    )

    getFriendsQuery.queryRes.map(friendRow => {
        friends.push(friendRow.friend)
    });
    
    res.json({
        status: getFriendsQuery.status,
        friends: friends
    });
})

app.post('/retrieve_public', async (req, res) => {
    let statusCode;
    let public_photos = {}

    let retrievePublicPhotos = await sendQuery(`
        SELECT image_name as name, ST_X(location) as lng, ST_Y(location) as lat, author as author
        FROM images
        WHERE
            author <> \'${req.body.logged_user}\' AND
            public = true`
    )

    let index = 0;
    retrievePublicPhotos.queryRes.map(row => {
        let tmp_img = {};
        tmp_img.name = row.name
        tmp_img.coords = [row.lat, row.lng]
        tmp_img.author = row.author
        public_photos[index] = tmp_img
        index++;
    })

    res.json({
        status: retrievePublicPhotos.status,
        public_photos: public_photos
    });

})

app.post('/add_friend', async (req, res) => {

    let statusCode;

    let addFriendQuery = await sendQuery(`
        SELECT user_2
        FROM friendships
        WHERE
            user_1=\'${req.body.loggedUser}\' AND
            user_2=\'${req.body.newFriend}\';`
    )

    if (addFriendQuery.queryRes.length > 0) {
        statusCode = 409; // Already friends
    } else {
        
        let firstFriendRequest = await sendQuery(`
            INSERT INTO friendships (user_1, user_2)
            VALUES (\'${req.body.loggedUser}\', \'${req.body.newFriend}\');`
        )

        let secondFriendRequest = await sendQuery(`
            INSERT INTO friendships (user_1, user_2)
            VALUES (\'${req.body.newFriend}\', \'${req.body.loggedUser}\');`
        )

        if (firstFriendRequest.status == 200 && secondFriendRequest.status == 200) {
            statusCode = 200
        } else {
            statusCode = 401
        }
    }

    res.json({ status: statusCode });
})

app.post('/remove_friend', async (req, res) => {

    let statusCode;

    let removeFirstFriend = await sendQuery(`
        DELETE
        FROM friendships
        WHERE
            user_1=\'${req.body.logged_user}\' AND
            user_2=\'${req.body.friend}\';`
    )

    let removeSecondFriend = await sendQuery(`
        DELETE
        FROM friendships
        WHERE
            user_1=\'${req.body.friend}\' AND
            user_2=\'${req.body.logged_user}\';`
    )

    if (removeFirstFriend.status == 200 && removeSecondFriend.status == 200) {
        statusCode = 200
    } else {
        statusCode = 401
    }

    res.json({ status: statusCode });
})

// Only used to display collection names on dashboard page
app.post('/retrievecollections', async (req, res) => {
    let statusCode;
    let retrieved_collections = [];

    
    // Distinct to avoid multi-photo collections to duplicate collection name
    // Also does not load collections with no photos in.
    const retrieveUserCollectionsQuery = await sendQuery(`
        SELECT DISTINCT c.collection_name as name, i.place as place
        FROM collections as c, images as i
        WHERE 
            c.collection_name = i.reference AND
            i.author=\'${req.body.logged_user}\'`
    )

    retrieveUserCollectionsQuery.queryRes.map(collection => {
        let tmp_coll = {}
        tmp_coll['name'] = collection.name
        tmp_coll['place'] = collection.place
        retrieved_collections.push(tmp_coll)
    })

    // Photos in which the user is tagged
    let taggedCollectionsQuery = await sendQuery(`
        SELECT reference as name, place as place
        FROM images
        WHERE
            author <> \'${req.body.logged_user}\' AND
            \'${req.body.friend}\' = ANY(tagged_people)`
    )

    taggedCollectionsQuery.queryRes.map(collection => {
        let tmp_coll = {}
        tmp_coll['name'] = collection.name
        tmp_coll['place'] = collection.place
        retrieved_collections.push(tmp_coll)
    })

    if (retrieveUserCollectionsQuery.status == 200 && taggedCollectionsQuery.status == 200) {
        statusCode = 200
    } else {
        statusCode = 401
    }

    res.json({
        status: statusCode,
        retrieved_collections: retrieved_collections
    })
})

app.post('/login', async (req, res) => {

    let statusCode;

    let checkLoginQuery = await sendQuery(`
        SELECT username
        FROM users
        WHERE
            username=\'${req.body.username}\' AND
            password=\'${req.body.password}\'`
    )
    
    if (checkLoginQuery.queryRes.length == 1) {
        statusCode = 200;
    } else {
        statusCode = 401;
    }

    res.json({
        status: statusCode
    })
})

app.post('/signup', async (req, res) => {

    let signupQuery = await sendQuery(`
        INSERT INTO users (name, surname, username, password)
        VALUES (\'${req.body.name}\', \'${req.body.surname}\', \'${req.body.username}\', \'${req.body.password}\');`
    )

    res.json({
        status: signupQuery.status
    })

})

app.listen(port, () => {
    console.log('Server on');
})
