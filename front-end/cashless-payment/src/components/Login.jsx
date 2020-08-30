import React, {useState} from "react";
import {Button, FormGroup, FormControl, FormLabel} from "react-bootstrap";
import axios from "axios";
import {useHistory} from "react-router-dom";

export default function Login(props) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const history = useHistory();

    function validateForm() {
        return email.length > 0 && password.length > 0;
    }

    function handleSubmit(event) {
        event.preventDefault();
        const postData = {
            email: email,
            password: password
        }

        axios.post(`http://localhost:3000/login`, postData)
            .then(
                res => {
                    const response = res.data;
                    const token = res.headers["auth-token"];
                    if (token !== undefined) {
                        props.handleLogin(response, token);
                        history.push("/")
                    } else {
                        alert("Login failed");
                    }
                }
            ).catch(
            err => {
                if (err.response !== undefined && err.response.data.message !== undefined) {
                    alert(err.response.data.message);
                }
                console.log(err.response);
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
                <FormGroup controlId="password" bssize="large">
                    <FormLabel>Password</FormLabel>
                    <FormControl
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        type="password"
                    />
                </FormGroup>
                <Button block bssize="large" disabled={!validateForm()} type="submit">
                    Login
                </Button>
            </form>
        </div>
    );
}