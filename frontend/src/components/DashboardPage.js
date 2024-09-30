import React from "react";
import {
  Container,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
} from "@mui/material";

function DashboardPage() {
  return (
    <Container maxWidth="lg" style={{ marginTop: "50px" }}>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>

      {/* First Row of Cards */}
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h5">Metric 1</Typography>
              <Typography variant="body2">Data or Stats Here</Typography>
            </CardContent>
            <CardActions>
              <Button size="small" color="primary">
                Details
              </Button>
            </CardActions>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h5">Metric 2</Typography>
              <Typography variant="body2">Data or Stats Here</Typography>
            </CardContent>
            <CardActions>
              <Button size="small" color="primary">
                Details
              </Button>
            </CardActions>
          </Card>
        </Grid>
        {/* Add more cards as needed */}
      </Grid>

      {/* Charts or other elements */}
      <Grid container spacing={3} style={{ marginTop: "30px" }}>
        <Grid item xs={12} md={6}>
          <Paper style={{ padding: "20px" }}>
            <Typography variant="h6">Chart 1</Typography>
            {/* Add a chart component here */}
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper style={{ padding: "20px" }}>
            <Typography variant="h6">Chart 2</Typography>
            {/* Add a chart component here */}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}

export default DashboardPage;
