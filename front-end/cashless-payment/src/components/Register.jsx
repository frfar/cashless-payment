import React, {useState} from "react";
import {Button, FormGroup, FormControl, FormLabel} from "react-bootstrap";
import axios from "axios";

export default function Register(props) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [contact, setContact] = useState("");

    function validateForm() {
        return email.length > 0 && password.length > 0 && name.length > 0 && contact.length > 0;
    }

    function handleSubmit(event) {
        event.preventDefault();
        const postData = {
            email: email,
            password: password,
            name: name,
            contact: contact
        }

        axios.post(`http://localhost:3000/register`, postData, {
            headers: { "Auth-Token": props.userToken }
        })
            .then(
                res => {
                    const response = res.data;
                    if (response.message === "User with email already exists") {
                        alert(response.message);
                    } else if (response.message.startsWith("User created")){
                        alert(response.message);
                        setEmail("");
                        setContact("");
                        setName("");
                        setPassword("");
                    }
                }
            ).catch(
            err => {
                alert(err.response);
            }
        )


    }

    return (
        <div className="Login">
            <form onSubmit={handleSubmit}>
                <FormGroup controlId="email" bssize="large">
                    <FormLabel>Email</FormLabel>
                    <FormControl
                        autoFocus
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                    />
                </FormGroup>
                <FormGroup controlId="name" bssize="large">
                    <FormLabel>Name</FormLabel>
                    <FormControl
                        type="text"
                        value={name}
                        onChange={e => setName(e.target.value)}
                    />
                </FormGroup>
                <FormGroup controlId="contact" bssize="large">
                    <FormLabel>Contact</FormLabel>
                    <FormControl
                        type="text"
                        value={contact}
                        onChange={e => setContact(e.target.value)}
                    />
                </FormGroup>
                <FormGroup controlId="password" bssize="large">
                    <FormLabel>Password</FormLabel>
                    <FormControl
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        type="password"
                    />
                </FormGroup>
                <Button block bssize="large" disabled={!validateForm()} type="submit">
                    Register
                </Button>
            </form>
        </div>
    );
}
