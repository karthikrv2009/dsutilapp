import React from "react";
import { Box, Typography, Container, Grid, Link } from "@mui/material";

function Footer() {
  return (
    <Box
      component="footer"
      sx={{
        py: 3,
        px: 2,
        mt: "auto",
        backgroundColor: (theme) =>
          theme.palette.mode === "light"
            ? theme.palette.grey[200]
            : theme.palette.grey[800],
      }}
    >
      <Container maxWidth="lg">
        <Grid container spacing={4}>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="h6" gutterBottom>
              Company
            </Typography>
            <Link href="#" variant="body2" color="inherit" display="block">
              About Us
            </Link>
            <Link href="#" variant="body2" color="inherit" display="block">
              Careers
            </Link>
            <Link href="#" variant="body2" color="inherit" display="block">
              Contact Us
            </Link>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="h6" gutterBottom>
              Resources
            </Typography>
            <Link href="#" variant="body2" color="inherit" display="block">
              Blog
            </Link>
            <Link href="#" variant="body2" color="inherit" display="block">
              Documentation
            </Link>
            <Link href="#" variant="body2" color="inherit" display="block">
              Support
            </Link>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="h6" gutterBottom>
              Legal
            </Typography>
            <Link href="#" variant="body2" color="inherit" display="block">
              Privacy Policy
            </Link>
            <Link href="#" variant="body2" color="inherit" display="block">
              Terms of Service
            </Link>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Typography variant="h6" gutterBottom>
              Follow Us
            </Typography>
            <Link href="#" variant="body2" color="inherit" display="block">
              Facebook
            </Link>
            <Link href="#" variant="body2" color="inherit" display="block">
              Twitter
            </Link>
            <Link href="#" variant="body2" color="inherit" display="block">
              LinkedIn
            </Link>
          </Grid>
        </Grid>
        <Box mt={4} textAlign="center">
          <Typography variant="body2" color="textSecondary">
            DataPig © 2025. All rights reserved.
          </Typography>
        </Box>
      </Container>
    </Box>
  );
}

export default Footer;
