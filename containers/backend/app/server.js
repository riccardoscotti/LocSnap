const express = require('express');
const multer = require('multer');
const path = require('path');
const bodyParser = require('body-parser');
const { Client } = require('pg');

const app = express();

app.use(bodyParser.json({limit: '50mb'}));

const storage = multer.diskStorage({
    destination: './upload/images',
    filename: (req, file, cb) => {
        return cb(null, `${file.fieldname}_${Date.now()}${path.extname(file.originalname)}`);
    },
})

const upload = multer({
    storage: storage
});
const port = 8080;

app.post('/test', upload.single('picture'), (req, res) => {
    res.send("Richiesta POST effettuata.")
    console.log(req.file);

    res.sendStatus(200);
})

app.post('/imageupload', upload.single('image'), (req, res) => {

  console.log("Uploading...")

  const name = req.body.name
  const image = req.body.image

  console.log("Uploaded photo: ", name)
  console.log("Name: ", name)

  res.json({ status: '200' })
    
});

app.get('/test', (req, res) => {
    res.send("Richiesta GET effettuata.")

    res.sendStatus(200);
})

app.post('/login', (req, res) => {

    console.log("User login request with: ", req.body.username, req.body.password)

    const client = new Client({
        user: 'postgres',
        host: '0.0.0.0',
        database: 'user_login',
        //password: 'contextawarerc',
        port: 5432,
    });

    client.connect();

    query = `INSERT INTO users (nome, cognome, username, password) VALUES (\'---\', \'---\', \'${req.body.username}\', \'${req.body.password}\');`

    client.query(query, (err, res) => {
        console.log(err, res);
        client.end();
    });

    const data = { status: '200' }
    res.json(data)
})

app.listen(port, () => {
    console.log('Server on');
})
