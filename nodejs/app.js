const aesjs = require('aes-js');
const eccrypto = require('eccrypto');
const crypto = require('crypto');
const chalk = require('chalk');

// what is aes block cipher?
// https://searchsecurity.techtarget.com/definition/Advanced-Encryption-Standard
var key_256 = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
    29, 30, 31];
//126 bits (32bytes)  -> 128? 192? the longer the more security?
//what is a key? key ~ password? -> sender and receiver must have same key
let key = new Uint8Array(key_256);

//convert text to bytes
const text = '1234';
const textBytes = aesjs.utils.utf8.toBytes(text);

// CTR(Counter Mode) counter? what is it?
// also check CBC CFB
// https://www.youtube.com/watch?v=rE1IF4QrxIM

let aesCtr = new aesjs.ModeOfOperation.ctr(key, new aesjs.Counter());
const encryptedBytes = aesCtr.encrypt(textBytes);
console.log(textBytes);
console.log(chalk.green(encryptedBytes));

// convert to hex for storing or print
const encryptedHex = aesjs.utils.hex.fromBytes(encryptedBytes);
console.log(Buffer.from(encryptedBytes).toString('base64').length);
console.log(chalk.red(encryptedHex));

// decrypt
//convert back to byte
const backToBytes = aesjs.utils.hex.toBytes(encryptedHex);

//create nenw counter since old counter remains at old state
const decryptCtr = new aesjs.ModeOfOperation.ctr(key, new aesjs.Counter());
const decryptedBytes = decryptCtr.decrypt(backToBytes);

const decryptedText = aesjs.utils.utf8.fromBytes(decryptedBytes);
console.log(chalk.yellow(decryptedText));

// ECDSA
// https://blog.cloudflare.com/a-relatively-easy-to-understand-primer-on-elliptic-curve-cryptography/
// https://blog.cloudflare.com/ecdsa-the-digital-signature-algorithm-of-a-better-internet/
// https://en.wikipedia.org/wiki/Elliptic_Curve_Digital_Signature_Algorithm

//random 32-byte private key
const privateKey = eccrypto.generatePrivate();
console.log(privateKey);
// coressponding uncompressed (65-byte) public key
const publicKey = eccrypto.getPublic(privateKey);
console.log(publicKey);

//hash message to sign
const msg = crypto.createHash("sha256").update(encryptedHex).digest();
 
eccrypto.sign(privateKey, msg).then((sig) => {
    console.log("Signature in DER format: ", sig.toString('base64').length);
    eccrypto.verify(publicKey, msg, sig).then(()=>{
        console.log("Signature is OK");
    }).catch(()=>{
        console.log("Signature is BAD");
    });
});


