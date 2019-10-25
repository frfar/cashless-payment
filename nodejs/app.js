const aesjs = require('aes-js');
const eccrypto = require('eccrypto');
const crypto = require('crypto');
const chalk = require('chalk');
const express = require('express');
const app = express();

var key_256 = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
    29, 30, 31];

let key = new Uint8Array(key_256);

//random 32-byte private key
const privateKey = eccrypto.generatePrivate();

const publicKey = eccrypto.getPublic(privateKey);

app.get('/', (req, res) => {
    res.send('Hello World')
})
   
app.get('/demo', (req, res) => {
    const text = req.query.trans;
    const textBytes = aesjs.utils.utf8.toBytes(text);
    console.log(chalk.yellow(text));
    let aesCtr = new aesjs.ModeOfOperation.ctr(key, new aesjs.Counter());
    const encryptedBytes = aesCtr.encrypt(textBytes);
    // add more bytes to encrypted byte
    const encryptedHex = aesjs.utils.hex.fromBytes(encryptedBytes);
    const msg = crypto.createHash("sha256").update(encryptedHex).digest();
 
    eccrypto.sign(privateKey, msg).then((sig) => {
        const ret = encryptedHex.toString('base64') + sig.toString('base64');
        console.log(sig.toString('base64'));
        res.status(200).send(ret);
    });
})

app.listen(3000, (err)=>{
    if (err){
        console.log(err);
        throw err;
    }
    console.log('Lisening on port 3000');
})
