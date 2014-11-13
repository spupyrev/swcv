package edu.cloudy.ui;

import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;
import edu.cloudy.metrics.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;

public class MetricsPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private WordGraph wordGraph;
    private LayoutResult layout;

    private JTextField wordCountField;
    private JTextField totalWeightField;
    private JTextField maxPlanarSubgraphField;
    private JTextField overlapsField;
    private JTextField adjacenciesField;
    private JTextField proximityField;

    private JTextField stressField;
    private JTextField distortionField;
    private JTextField spaceField;
    private JTextField spaceCHField;
    private JTextField uniformiltyField;
    private JTextField aspectField;
    private JTextField precisionField;
    private JTextField precisionRecallField;

    public MetricsPanel(WordGraph wordGraph, LayoutResult layout)
    {
        this.layout = layout;
        this.wordGraph = wordGraph;

        //staticstics
        JPanel statPanel = new JPanel();
        statPanel.setBorder(new javax.swing.border.TitledBorder("Statistics"));
        statPanel.setLayout(new GridLayout(4, 2));
        add(BorderLayout.NORTH, statPanel);

        addNumberOfWords(statPanel);
        addTotalWeight(statPanel);
        addMaxPlanarSubgraph(statPanel);
        addOverlaps(statPanel);

        //separator
        add(new JLabel(" "));

        //metrics
        JPanel metricsPanel = new JPanel();
        metricsPanel.setBorder(new javax.swing.border.TitledBorder("Quality Metrics"));
        metricsPanel.setLayout(new GridLayout(10, 2));
        add(BorderLayout.CENTER, metricsPanel);

        addAdjacencies(metricsPanel);
        addProximity(metricsPanel);
        addAdjacencies2(metricsPanel);
        addPrecision(metricsPanel);
        addPrecisionRecall(metricsPanel);
        addDistrortion(metricsPanel);
        addSpace(metricsPanel);
        addSpaceConvexHull(metricsPanel);
        addUniformity(metricsPanel);
        addAspect(metricsPanel);

        //dummy
        add(BorderLayout.SOUTH, new JPanel());

        analyse();

        int height = (int)statPanel.getPreferredSize().getHeight();
        statPanel.setPreferredSize(new Dimension(300, height));
        int width = (int)statPanel.getPreferredSize().getWidth();
        metricsPanel.setPreferredSize(new Dimension(width, (int)metricsPanel.getPreferredSize().getHeight()));
        setPreferredSize(new Dimension(width, 100));
    }

    private void addNumberOfWords(JPanel parent)
    {
        parent.add(new JLabel("#words"));
        wordCountField = createTextField();
        parent.add(wordCountField);
    }

    private void addTotalWeight(JPanel parent)
    {
        parent.add(new JLabel("total weight"));
        totalWeightField = createTextField();
        parent.add(totalWeightField);
    }

    private void addMaxPlanarSubgraph(JPanel parent)
    {
        parent.add(new JLabel("max planar subgraph"));
        maxPlanarSubgraphField = createTextField();
        parent.add(maxPlanarSubgraphField);
    }

    private void addOverlaps(JPanel parent)
    {
        parent.add(new JLabel("overlaps"));
        overlapsField = createTextField();
        parent.add(overlapsField);
    }

    private void addAdjacencies(JPanel parent)
    {
        parent.add(new JLabel("adjacencies realized"));
        adjacenciesField = createTextField();
        parent.add(adjacenciesField);
    }

    private void addProximity(JPanel parent)
    {
        parent.add(new JLabel("proximities realized"));
        proximityField = createTextField();
        parent.add(proximityField);
    }

    private void addPrecision(JPanel parent)
    {
        parent.add(new JLabel("precision"));
        precisionField = createTextField();
        parent.add(precisionField);
    }

    private void addPrecisionRecall(JPanel parent)
    {
        parent.add(new JLabel("precision/recall"));
        precisionRecallField = createTextField();
        parent.add(precisionRecallField);
    }

    private void addAdjacencies2(JPanel parent)
    {
        parent.add(new JLabel("stress"));
        stressField = createTextField();
        parent.add(stressField);
    }

    private void addDistrortion(JPanel parent)
    {
        parent.add(new JLabel("distortion"));
        distortionField = createTextField();
        parent.add(distortionField);
    }

    private void addSpace(JPanel parent)
    {
        parent.add(new JLabel("space (bounding box, %)"));
        spaceField = createTextField();
        parent.add(spaceField);
    }

    private void addSpaceConvexHull(JPanel parent)
    {
        parent.add(new JLabel("space (convex hull, %)"));
        spaceCHField = createTextField();
        parent.add(spaceCHField);
    }

    private void addUniformity(JPanel parent)
    {
        parent.add(new JLabel("uniformity"));
        uniformiltyField = createTextField();
        parent.add(uniformiltyField);
    }

    private void addAspect(JPanel parent)
    {
        parent.add(new JLabel("aspect ratio"));
        aspectField = createTextField();
        parent.add(aspectField);
    }

    private JTextField createTextField()
    {
        JTextField field = new JTextField();
        field.setEnabled(false);
        field.setDisabledTextColor(Color.BLACK);
        field.setPreferredSize(new Dimension(100, 20));
        field.setHorizontalAlignment(JTextField.CENTER);
        return field;
    }

    public void analyse()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat df4 = new DecimalFormat("#.####");

        wordCountField.setText("" + wordGraph.getWords().size());
        double totalWeight = new TotalWeightMetric().getValue(wordGraph, layout);
        totalWeightField.setText(df.format(totalWeight));
        maxPlanarSubgraphField.setText("<=" + df.format(new MaxPlanarSubgraphMetric().getValue(wordGraph, layout)));
        overlapsField.setText(new OverlapsMetric().getValue(wordGraph, layout) > 0.5 ? "yes" : "none");

        AdjacenciesMetric am = new AdjacenciesMetric();
        double adj = am.getValue(wordGraph, layout);

        adjacenciesField.setText(df4.format(adj / totalWeight));
        proximityField.setText(df4.format(new ProximityMetric().getValue(wordGraph, layout) / totalWeight));
        precisionField.setText(df.format(new PrecisionRecallMetric1().getValue(wordGraph, layout)));
        precisionRecallField.setText(df.format(new PrecisionRecallMetric2().getValue(wordGraph, layout)));
        stressField.setText(df.format(new StressMetric().getValue(wordGraph, layout)));
        distortionField.setText(df4.format(new DistortionMetric().getValue(wordGraph, layout)));
        spaceField.setText(df4.format(new SpaceMetric(false).getValue(wordGraph, layout)));
        spaceCHField.setText(df4.format(new SpaceMetric(true).getValue(wordGraph, layout)));
        uniformiltyField.setText(df4.format(new UniformAreaMetric().getValue(wordGraph, layout)));
        aspectField.setText(df4.format(new AspectRatioMetric().getValue(wordGraph, layout)));
    }

}
