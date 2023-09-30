import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int quantityX = readNumber('X');
        int quantityY = readNumber('Y');
        BigDecimal[] xy = readProductEnsembles(quantityX * quantityY);

        BigDecimal[] x = findProbabilitiesX(quantityX, quantityY, xy);
        BigDecimal[] y = findProbabilitiesY(quantityY, quantityX, xy);
        writeProbabilities(x, 'x');
        writeProbabilities(y, 'y');

        checkEnsemblesForIndependence(x, y, xy);

        BigDecimal[] xIy = findConditionalProbabilitiesXIY(xy, y, quantityX, quantityY);
        BigDecimal[] yIx = findConditionalProbabilitiesYIX(xy, x, quantityX, quantityY);
        writeConditionalProbabilities(xIy, 'x', 'y', quantityX, quantityY);
        writeConditionalProbabilities(yIx, 'y', 'x', quantityY, quantityX);

        findEntropy(x, "X");
        findEntropy(y, "Y");
        findEntropy(xy, "XY");

        findConditionalEntropyX(xIy, y, quantityX, quantityY);
        findConditionalEntropyY(yIx, x, quantityX, quantityY);
    }

    private static int readNumber(char ensemble) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите количество элементов ансамбля " + ensemble + ": ");
        return scanner.nextInt();
    }

    private static BigDecimal[] readProductEnsembles(int length) {
        System.out.println("Введите произведение ансамблей XY: ");

        Scanner scanner = new Scanner(System.in);
        BigDecimal[] xy = new BigDecimal[length];

        for (int i = 0; i < length; i++)
            xy[i] = scanner.nextBigDecimal();

        return xy;
    }

    private static BigDecimal[] findProbabilitiesX(int length, int numberTerms, BigDecimal[] xy) {
        BigDecimal[] x = new BigDecimal[length];

        for (int i = 0; i < length; i++) {
            BigDecimal sum = BigDecimal.valueOf(0);

            for (int j = 0; j < numberTerms; j++)
                sum = sum.add(xy[i * numberTerms + j]);

            x[i] = sum;
        }

        return x;
    }

    private static BigDecimal[] findProbabilitiesY(int length, int numberTerms, BigDecimal[] xy) {
        BigDecimal[] y = new BigDecimal[length];

        for (int i = 0; i < length; i++) {
            BigDecimal sum = BigDecimal.valueOf(0);

            for (int j = 0; j < numberTerms; j++)
                sum = sum.add(xy[i + j * length]);

            y[i] = sum;
        }

        return y;
    }

    private static void writeProbabilities(BigDecimal[] probabilities, char ensemble) {
        System.out.println();

        for (int i = 0; i < probabilities.length; i++)
            System.out.println("Вероятность p(" + ensemble + "_" + (i + 1) + "): " + probabilities[i].stripTrailingZeros());
    }


    private static void checkEnsemblesForIndependence(BigDecimal[] x, BigDecimal[] y, BigDecimal[] xy) {
        System.out.println();

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < y.length; j++) {
                if (!xy[i * y.length + j].stripTrailingZeros().equals(x[i].multiply(y[j]).stripTrailingZeros())) {
                    System.out.println("Ансамбли X и Y зависимы");
                    return;
                }
            }
        }

        System.out.println("Ансамбли X и Y независимы");
    }

    private static BigDecimal[] findConditionalProbabilitiesXIY(
            BigDecimal[] xy, BigDecimal[] y, int quantityX, int quantityY
    ) {
        BigDecimal[] xIy = new BigDecimal[xy.length];

        for (int i = 0; i < quantityX; i++) {
            for (int j = 0; j < quantityY; j++) {
                xIy[i * quantityY + j] = xy[i * quantityY + j].divide(y[j], new MathContext(4, RoundingMode.HALF_UP));
            }
        }

        return xIy;
    }

    private static BigDecimal[] findConditionalProbabilitiesYIX(
            BigDecimal[] xy, BigDecimal[] x, int quantityX, int quantityY
    ) {
        BigDecimal[] yIx = new BigDecimal[xy.length];

        for (int i = 0; i < quantityY; i++) {
            for (int j = 0; j < quantityX; j++) {
                yIx[i * quantityX + j] = xy[j * quantityY + i].divide(x[j], new MathContext(4, RoundingMode.HALF_UP));
            }
        }

        return yIx;
    }

    private static void writeConditionalProbabilities(BigDecimal[] probabilities, char firstEnsemble, char secondEnsemble,
                                                      int firstLength, int secondLength) {
        System.out.println();

        for (int i = 0; i < firstLength; i++)
            for (int j = 0; j < secondLength; j++)
                System.out.println("Условная вероятность p(" + firstEnsemble + "_" + (i + 1) + "|" + secondEnsemble +
                        "_" + (j + 1) + "): " + probabilities[i * secondLength + j].stripTrailingZeros());
    }

    private static void findEntropy(BigDecimal[] probabilities, String ensemble) {
        BigDecimal entropy = BigDecimal.valueOf(0);

        double ln2 = Math.log(2);

        for (BigDecimal probability : probabilities) {
            double value = probability.doubleValue();
            double ln = Math.log(value);
            entropy = entropy.add(probability.multiply(BigDecimal.valueOf(ln / ln2)));
        }

        entropy = entropy.multiply(BigDecimal.valueOf(-1)).stripTrailingZeros();

        System.out.println();
        System.out.println("Энтропия H(" + ensemble + "): " + entropy);
    }

    private static void findConditionalEntropyX(BigDecimal[] xIy, BigDecimal[] y, int quantityX, int quantityY) {
        BigDecimal h_yX = BigDecimal.valueOf(0);
        double ln2 = Math.log(2);

        for (int i = 0; i < quantityY; i++) {
            BigDecimal h_y_iX = BigDecimal.valueOf(0);

            for (int j = 0; j < quantityX; j++) {
                BigDecimal current = xIy[i + j * quantityY];
                double value = current.doubleValue();
                double ln = Math.log(value);
                h_y_iX = h_y_iX.add(current.multiply(BigDecimal.valueOf(ln / ln2)));
            }

            h_y_iX = h_y_iX.multiply(BigDecimal.valueOf(-1));

            h_yX = h_yX.add(y[i].multiply(h_y_iX)).stripTrailingZeros();
        }

        System.out.println();
        System.out.println("Условная энтропия H_y(X): " + h_yX);
    }

    private static void findConditionalEntropyY(BigDecimal[] yIx, BigDecimal[] x, int quantityX, int quantityY) {
        BigDecimal h_xY = BigDecimal.valueOf(0);
        double ln2 = Math.log(2);

        for (int i = 0; i < quantityX; i++) {
            BigDecimal h_x_iY = BigDecimal.valueOf(0);

            for (int j = 0; j < quantityY; j++) {
                BigDecimal current = yIx[i + quantityX * j];
                double value = current.doubleValue();
                double ln = Math.log(value);
                h_x_iY = h_x_iY.add(current.multiply(BigDecimal.valueOf(ln / ln2)));
            }

            h_x_iY = h_x_iY.multiply(BigDecimal.valueOf(-1));

            h_xY = h_xY.add(x[i].multiply(h_x_iY)).stripTrailingZeros();
        }

        System.out.println();
        System.out.println("Условная энтропия H_x(Y): " + h_xY);
    }
}

// 0,21 0,42 0,07 0,09 0,18 0,03
// 0,14 0,35 0,21 0,06 0,15 0,09
// 0,4 0,3 0,2 0,1