import React from "react";
import {Redirect, Route, Switch} from "react-router-dom";
import Home from "./components/Home";
import NotFound from "./components/404";
import Login from "./components/Login";
import Register from "./components/Register";
import ChangePassword from "./components/ChangePassword";

export default function Routes(props) {
    const data = props.data;
    const handleLogin = props.handleLogin;
    const isLoggedIn = props.isLoggedIn;
    const isAdmin = props.isAdmin;
    const token = props.userToken;
    return (
        <Switch>
            <Route exact path="/" render={(props) => isLoggedIn ? (<Home {...props} data={data} isAdmin={isAdmin}/>) : (<Redirect to="/login"/>)} />
            <Route exact path="/login" render={(props) => <Login {...props} handleLogin={handleLogin}/>}/>
            <Route exact path="/register" render={(props) => isAdmin ? (<Register {...props} userToken={token}/>) : (<Redirect to="/" />)}/>
            <Route exact path="/changePassword" render={(props) => <ChangePassword {...props} userToken={token} />}/>
            <Route>
                <NotFound />
            </Route>
        </Switch>
    );
}