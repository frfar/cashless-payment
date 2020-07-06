import React, {useState} from "react";
import {Button, FormGroup, FormControl, FormLabel} from "react-bootstrap";
import axios from "axios";
import {useHistory} from "react-router-dom";

export default function ChangePassword(props) {
    const [password, setPassword] = useState("");

    const history = useHistory();

    function validateForm() {
        return password.length > 0;
    }

    function handleSubmit(event) {
        event.preventDefault();
        const postData = {
            newPassword: password
        };

        axios.post(`http://localhost:3000/changePassword`, postData, {
            headers: { "Auth-Token": props.userToken }
        })
            .then(
                res => {
                    const response = res.data;
                    alert(response.message);
                    history.push("/");
                }
            ).catch(
            err => {
                alert(err.response);
                console.log(err.response)
            }
        )


    }

    return (
        <div className="ChangePassword">
            <form onSubmit={handleSubmit}>
                <FormGroup controlId="password" bssize="large">
                    <FormLabel>New Password</FormLabel>
                    <FormControl
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        type="password"
                    />
                </FormGroup>
                <Button block bssize="large" disabled={!validateForm()} type="submit">
                    Change Password
                </Button>
            </form>
        </div>
    );
}