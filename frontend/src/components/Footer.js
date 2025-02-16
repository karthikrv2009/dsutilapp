import React from "react";
import { Box, Typography, Container, Grid, Link } from "@mui/material";

function Footer() {
  return (
    <Box
      component="footer"
      sx={{
        py: 2,
        px: 2,
        mt: "auto",
        backgroundColor: "background.default",
      }}
    >
      <Container maxWidth="lg">
        <Box mt={2} textAlign="center">
          <Typography variant="body2" color="textSecondary">
            DataPig LLC © 2025. All rights reserved.
          </Typography>
        </Box>
      </Container>
    </Box>
  );
}

export default Footer;
