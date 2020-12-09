import React from 'react'
import {Link} from "react-router-dom";
import {Navbar, Nav, Button} from "react-bootstrap";
import "./App.css";
import 'bootstrap/dist/css/bootstrap.min.css';
import Routes from "./Routes";
import Container from "react-bootstrap/Container";


class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isLoggedIn: false,
            isAdmin: false,
            userToken: '',
            userInfo: {},
            amount: "",
            vendID: ""

        }
        this.handleTransaction = this.handleTransaction.bind(this);
        this.handleLogin = this.handleLogin.bind(this);
        this.handleLogout = this.handleLogout.bind(this);
    }

    handleLogin(userInfo, userToken){

        this.setState({
            userToken: userToken,
            userInfo: userInfo,
            isAdmin: userInfo.isAdmin,
            isLoggedIn: true
        })
    }

    handleTransaction(amount1, vendID1){

        this.setState({
            amount: amount1,
            vendID: vendID1
        })
    }

    handleLogout() {
        this.setState({
            userToken: '',
            isLoggedIn: false,
            isAdmin: false
        })
    }

    loginButton(isLoggedIn, handleLogout){
        if (isLoggedIn){
            return (
                <Nav.Item onClick={handleLogout}>
                    <Button variant="link">Logout</Button>
                </Nav.Item>
            )
        } else {
            return (
                <Nav.Item>
                    <Link to="/login">
                        Login
                    </Link>
                </Nav.Item>
            )
        }
    }

    render() {
        const isLoggedIn = this.state.isLoggedIn;
        return (
            <Container className="App">
                <Navbar fluid="true" bg="light">

                    <Navbar.Brand>
                        <Link to="/">Cashless-Payment</Link>
                    </Navbar.Brand>
                    <Nav className="mr-auto">
                    </Nav>

                    <Nav>
                        {this.loginButton(isLoggedIn, this.handleLogout)}
                    </Nav>
                    <Navbar.Toggle/>

                </Navbar>
                <Routes
                    isLoggedIn={isLoggedIn}
                    isAdmin={this.state.isAdmin}
                    userToken = {this.state.userToken}
                    handleLogin={(userInfo, userToken) => this.handleLogin(userInfo, userToken)}
                    handleTransaction={(amount, vendID) => this.handleTransaction(amount, vendID)}
                />
            </Container>

        )
    }
}

export default App
