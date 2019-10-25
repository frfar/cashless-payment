const aesjs = require('aes-js');
const eccrypto = require('eccrypto');
const crypto = require('crypto');
const chalk = require('chalk');
const express = require('express');
const app = express();

//32 bytes key for aes
var key_256 = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
    29, 30, 31];

let key = new Uint8Array(key_256);

//random 32-byte private key for ecc
const privateKey = eccrypto.generatePrivate();
const publicKey = eccrypto.getPublic(privateKey);

app.get('/', (req, res) => {
    res.send('Hello World')
})
   
// demo1 based on documents of npm aes-js and eccrypto
app.get('/demo1', (req, res) => {
    const text = req.query.trans;
    const textBytes = aesjs.utils.utf8.toBytes(text);
    console.log(chalk.yellow(text));
    let aesCtr = new aesjs.ModeOfOperation.ctr(key, new aesjs.Counter());
    const encryptedBytes = aesCtr.encrypt(textBytes);
    // add more bytes to encrypted byte: not done
    const encryptedHex = aesjs.utils.hex.fromBytes(encryptedBytes);
    const msg = crypto.createHash("sha256").update(encryptedHex).digest();
 
    eccrypto.sign(privateKey, msg).then((sig) => {
        const ret = encryptedHex.toString('base64') + sig.toString('base64');
        console.log(sig.toString('base64'));
        res.status(200).send(ret);
    });
})



// demo 2 based on 
// https://stackoverflow.com/questions/50922462/aes-cbc-pkcs5padding-iv-decryption-in-nodejs-encrypted-in-java/50925146
// https://stackoverflow.com/questions/12710001/how-to-convert-uint8-array-to-base64-encoded-string
// https://stackoverflow.com/questions/23097928/node-js-btoa-is-not-defined-error

//convert key wich is utf8 array to string based 64
const key2 = Buffer.from(String.fromCharCode.apply(null, key)).toString('base64');
//iv based 64 | should random every time?
const iv = crypto.randomBytes(16).toString('base64');

console.log(chalk.yellow('keyBase64 is : ' + key2));
console.log(chalk.yellow('ivBase64 is : ' + iv.toString('base64')));

app.get('/demo2', (req,res) => {
    const text = req.query.trans;

    //cipherText is a string
    const encryptedText = encrypt(text,key2,iv);
    //console.log(encryptedText.length)
    // encryptedText length = 24 bytes, which is 3/4 of length of 32 bytes key
    //decrypt test
    console.log(chalk.yellow('decrypt: ' + decrypt(encryptedText,key2,iv)));

    //convert to buffer or hash?
    const msg = crypto.createHash("sha256").update(encryptedText).digest();

    eccrypto.sign(privateKey, msg).then((sig) => {
        const ret = encryptedText.toString('base64') + sig.toString('base64');
        console.log(sig.toString('base64'));
        res.status(200).send(ret);
    });
})

const encrypt = (plainText, keyBase64, ivBase64) => {

    const key = Buffer.from(keyBase64, 'base64');
    const iv = Buffer.from(ivBase64, 'base64');

    const cipher = crypto.createCipheriv('aes-256-cbc', key, iv);
    let encrypted = cipher.update(plainText, 'utf8', 'base64')
    encrypted += cipher.final('base64');
    return encrypted;
};

const decrypt = (messagebase64, keyBase64, ivBase64) => {

    const key = Buffer.from(keyBase64, 'base64');
    const iv = Buffer.from(ivBase64, 'base64');

    const decipher = crypto.createDecipheriv('aes-256-cbc', key, iv);
    let decrypted = decipher.update(messagebase64, 'base64');
    decrypted += decipher.final();
    return decrypted;
}

app.listen(3000, (err) => {
    if (err){
        console.log(err);
        throw err;
    }
    console.log('Lisening on port 3000');
})
