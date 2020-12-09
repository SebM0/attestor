package net.smnappz.attestor;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FirstFragment extends Fragment {
    private CountDownTimer countDownTimer;
    private Location initialLocation = null;
    private Instant initialInstant = null;
    private boolean started = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setArguments(((MainActivity)requireActivity()).getOptions());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.attestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateAttestation();
            }
        });
        view.findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });
        view.findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1_000) {

            // This is called after every 10 sec interval.
            public void onTick(long millisUntilFinished) {
                updateTimer();
            }

            public void onFinish() {
                start();
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            countDownTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void generateAttestation() {
        Instant now = Instant.now();
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.of("Europe/Paris"));
        String[] datetime = DATE_TIME_FORMATTER.format(now).split(" ");
        String date = datetime[0];
        String time2 = datetime[1];
        String time1 = time2.replace(':', 'h');
        String qrText = String.format("Cree le: %s a %s;\n" +
                "Nom: MORENO;\n" +
                "Prenom: Sebastien;\n" +
                "Naissance: 31/03/1971 a Chatenay-Malabry;\n" +
                "Adresse: 35 avenue Victor Hugo 92140 Clamart;\n" +
                "Sortie: %s a %s;\n" +
                "Motifs: sport_animaux;", date, time1, date, time2);
        FirstFragment.this.getArguments().putString("QR", qrText);
        FirstFragment.this.getArguments().putString("date", date);
        FirstFragment.this.getArguments().putString("time", time2);
        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment);
    }

    private void startTimer() {
        if (initialInstant == null) {
            initialInstant = Instant.now();
        }
        if (initialLocation == null) {
            initialLocation = ((MainActivity)requireActivity()).getLocation();
        }
        if (started) {
            pauseTimer();
        } else {
            resumeTimer();
        }
    }

    private void resumeTimer() {
        countDownTimer.start();
        ((Button)getView().findViewById(R.id.startButton)).setText("STOP");
        started = true;
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        ((Button)getView().findViewById(R.id.startButton)).setText("START");
        started = false;
    }

    private void resetTimer() {
        pauseTimer();
        initialInstant = null;
        initialLocation = null;
        ((TextView)getView().findViewById(R.id.timerTextView)).setText("00:00:00");
        ((TextView)getView().findViewById(R.id.distanceTextView)).setText("- m");
    }

    private void updateTimer() {
        // Time
        Instant now = Instant.now();
        Duration duration = Duration.between(initialInstant, now);
        String fduration = String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutes(), duration.getSeconds()%60);
        ((TextView)getView().findViewById(R.id.timerTextView)).setText(fduration);
        // Distance
        Location loc = ((MainActivity)requireActivity()).getLocation();
        TextView distanceTextView = (TextView) getView().findViewById(R.id.distanceTextView);
        if (loc != null) {
            if (initialLocation == null) {
                initialLocation = loc;
                distanceTextView.setText("- m");
            } else {
                int distance = (int)loc.distanceTo(initialLocation);
                distanceTextView.setText(String.format("%d m", distance));
            }
        } else {
            distanceTextView.setText("?");
        }
    }
}