package autoservisi.Autoservisi.MakinaIme.activities.interfaces;

public interface INewBaseActivity extends IBaseActivity {
    boolean isInputValid();
    void saveToRealm();
}
