<template>
  <b-container>
    
    <b-row align-h="center">
      <b-col cols="6">
        <b-form @submit.prevent="onSubmit">
          <b-form-group label="Email" label-for="email">
            <b-form-input id="email" v-model="email" required placeholder="이메일"></b-form-input>
          </b-form-group>
          <b-form-group label="Password" label-for="password">
            <b-form-input type="password" id="password" v-model="password" required placeholder="비밀번호"></b-form-input>
          </b-form-group>
          <b-button type="submit" :disabled="invalidForm" block variant="primary">Log In</b-button>
        </b-form>
      </b-col>
    </b-row>

    <b-row align-h="center" v-if="message">
      <b-col cols="6">
        <b-alert show variant="warning" >{{message}}</b-alert>
      </b-col>
    </b-row>
    <b-row align-h="center" v-if="error">
      <b-col cols="6">
        <b-alert show variant="danger" >{{error}}</b-alert>
      </b-col>
    </b-row>

    <b-row align-h="center">
      <b-col cols="6">
        <b-button to="/signup" block variant="outline-primary">Sign up</b-button>
      </b-col>
    </b-row>

  </b-container>
</template>

<script>
import {auth, setAuthInHeader} from '../api'
export default {
  data() {
    return {
      email: '',
      password: '',
      error: '',
      message: '',
      rPath: ''
    }
  },
  computed: {
    invalidForm() {
      return !this.email || !this.password
    }
  },
  created() {
    this.rPath = this.$route.query.rPath || '/'
  },
  methods: {
    onSubmit() {
      auth.login(this.email, this.password)
        .then(data => {
          localStorage.setItem('token', data.accessToken)
          setAuthInHeader(data.accessToken)
          this.$router.push(this.rPath)
        })
        .catch(err => {
          this.message = err.data.message
          this.error = err.data.error
        })
    }
  }
}
</script>