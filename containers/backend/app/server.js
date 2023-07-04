const express = require('express');
const multer = require('multer');
const path = require('path');
const bodyParser = require('body-parser');

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

  console.log("Uploaded photo with: ")
  console.log(name, image)

  // if (typeof req.body === "undefined") {
  //   console.log("Undefined request!")
  // } else {
  //   console.log("Successo!")
  //   console.log(req.body);
  //   const { name, image } = req.body;
  //   const binaryString = atob(image);
  //   const len = binaryString.length;
  //   const bytes = new Uint8Array(len);
  //   for (let i = 0; i < len; ++i) {
  //     bytes[i] = binaryString.charCodeAt(i);
  //   }
  //   const blob = new Blob([bytes], { type: 'image/png' });
  //   createImageBitmap(blob).then(bitmap => {
  //     // fai qualcosa con l'oggetto Bitmap qui...
  //     res.sendStatus(200);
  //   }).catch(err => {
  //     console.error(err);
  //     res.sendStatus(500);
  //   });
  // }

    
  });

app.get('/test', (req, res) => {
    res.send("Richiesta GET effettuata.")

    res.sendStatus(200);
})

app.listen(port, () => {
    console.log('Server on');
})
