const express = require('express');
const multer = require('multer');
const path = require('path');

const app = express();

const storage = multer.diskStorage({
    destination: './upload/images',
    filename: (req, file, cb) => {
        return cb(null, `${file.fieldname}_${Date.now()}${path.extname(file.originalname)}`);
    }
})

const upload = multer({
    storage: storage
});
const port = 8080;

app.post('/upload', upload.single('picture'), (req, res) => {
    res.send("Richiesta POST effettuata.")
    console.log(req.file);

    res.sendStatus(200);
})

app.get('/upload', (req, res) => {
    res.send("Richiesta GET effettuata.")

    res.sendStatus(200);
})

app.listen(port, () => {
    console.log('Server on');
})
